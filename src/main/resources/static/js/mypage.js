document.addEventListener('DOMContentLoaded', function () {
    // 1. 기존 메뉴 클릭 이벤트 로직
    const menuItems = document.querySelectorAll('.menu-item');
    const subtabItems = document.querySelectorAll('.subtab-item');
    const subtabPanels = document.querySelectorAll('.subtab-panel');

    // ★ 스크롤스파이용: 클릭으로 인한 스무스 스크롤 중엔 옵저버가 active를 안 건드리게 하는 플래그
    let isProgrammaticScroll = false;
    let programmaticScrollTimer = null;

    function activateMenu(tab) {
        menuItems.forEach(m => m.classList.remove('active'));
        const menuBtn = document.querySelector(`.menu-item[data-tab="${tab}"]`);
        if (menuBtn) menuBtn.classList.add('active');
    }

    function activateSubtab(sub) {
        subtabItems.forEach(s => s.classList.remove('active'));
        subtabPanels.forEach(p => p.classList.remove('active'));
        const subBtn = document.querySelector(`.subtab-item[data-subtab="${sub}"]`);
        const subPanel = document.getElementById('subtab-' + sub);
        if (subBtn) subBtn.classList.add('active');
        if (subPanel) subPanel.classList.add('active');
    }

    menuItems.forEach(item => {
        item.addEventListener('click', function () {
            const tab = this.getAttribute('data-tab');
            activateMenu(tab);
            const target = document.getElementById('panel-' + tab);
            if (target) {
                // ★ 클릭으로 스크롤하는 동안엔 아래 스크롤스파이가 끼어들어 active를
                //   덮어쓰지 않도록 잠깐 꺼둠 (스무스 스크롤 애니메이션 시간만큼)
                isProgrammaticScroll = true;
                clearTimeout(programmaticScrollTimer);
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                programmaticScrollTimer = setTimeout(() => {
                    isProgrammaticScroll = false;
                }, 700);
            }
        });
    });

    subtabItems.forEach(item => {
        item.addEventListener('click', function () {
            activateSubtab(this.getAttribute('data-subtab'));
        });
    });

    // ★ 스크롤스파이: 사용자가 직접 스크롤할 때도 현재 보이는 섹션에 맞춰
    //   사이드바 메뉴 active 표시가 자동으로 따라 바뀌게 함
    //   (기존엔 클릭했을 때만 active가 바뀌고, 손으로 스크롤하면 안 바뀌던 문제)
    const contentPanels = document.querySelectorAll('.content-panel[id^="panel-"]');
    if (contentPanels.length > 0 && 'IntersectionObserver' in window) {
        const spy = new IntersectionObserver((entries) => {
            if (isProgrammaticScroll) return; // 클릭으로 스크롤 중이면 무시

            // 여러 섹션이 동시에 걸쳐 보일 수 있으므로, 화면 상단에 가장 가까운(=가장 위에 있는) 섹션을 active로 함
            const visible = entries.filter(e => e.isIntersecting);
            if (visible.length === 0) return;

            visible.sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top);
            const topId = visible[0].target.id.replace('panel-', '');
            activateMenu(topId);
        }, {
            root: null,
            // 상단 고정 네브바(약 64px) + 여유분만큼 위쪽을 당겨서, 섹션이 상단 영역에 걸리면 바로 active 되게 함
            rootMargin: '-96px 0px -60% 0px',
            threshold: 0
        });

        contentPanels.forEach(panel => spy.observe(panel));
    }

    // 2. 출석 달력 로직 (새로 추가/교체)
    window.currentViewDate = new Date();

    fetchAndRenderHeatmap(
        window.currentViewDate.getFullYear(),
        window.currentViewDate.getMonth()
    );
});

async function fetchAndRenderHeatmap(year, month) {
    try {
        const response = await fetch(`/api/attendance?year=${year}&month=${month + 1}`);
        const attendanceList = await response.json();
        const attendanceSet = new Set(attendanceList.map(a => a.date));
        renderCalendar(year, month, attendanceSet);
    } catch (e) {
        console.error("데이터 로딩 실패:", e);
    }
}

function renderCalendar(year, month, attendanceSet) {
    const grid = document.getElementById('calendarGrid');
    const title = document.getElementById('monthTitle');
    if (!grid) return;

    grid.innerHTML = '';
    if (title) title.innerText = `${year}년 ${month + 1}월`;

    const lastDay = new Date(year, month + 1, 0).getDate();
    const firstDay = new Date(year, month, 1).getDay();

    for (let i = 0; i < firstDay; i++) {
        const empty = document.createElement('div');
        empty.className = 'calendar-day empty';
        grid.appendChild(empty);
    }

for (let i = 1; i <= lastDay; i++) {

    const dayBox = document.createElement('div');
    dayBox.className = 'calendar-day';
    dayBox.innerText = i;

    // 해당 날짜 객체
    const currentDate = new Date(year, month, i);

    // 일요일 / 토요일
    if (currentDate.getDay() === 0) {
        dayBox.classList.add('sun');
    }

    if (currentDate.getDay() === 6) {
        dayBox.classList.add('sat');
    }

    // 오늘 표시
    const today = new Date();

    if (
        today.getFullYear() === year &&
        today.getMonth() === month &&
        today.getDate() === i
    ) {
        dayBox.classList.add('today');
    }

    // 출석 여부
    const dateStr =
        `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;

    if (attendanceSet.has(dateStr)) {
        dayBox.classList.add('active');
    }

    grid.appendChild(dayBox);
}
}

// 3. 버튼 이벤트 연결 (HTML 버튼에 onclick="changeMonth(-1)" 등이 연결되어 있어야 함)
window.changeMonth = function(delta) {
    // 여기서 날짜를 관리할 전역 변수가 필요할 수 있습니다
    if (typeof window.currentViewDate === 'undefined') {
        window.currentViewDate = new Date();
    }
    window.currentViewDate.setMonth(window.currentViewDate.getMonth() + delta);
    fetchAndRenderHeatmap(window.currentViewDate.getFullYear(), window.currentViewDate.getMonth());
};

// 3-1. 주간 출석 그리드 렌더링 (누락되어 있던 함수 - 추가)
function renderWeeklyGrid(weeklyAttendance) {
    const grid = document.getElementById('weeklyGrid');
    if (!grid) return;

    grid.innerHTML = '';
    weeklyAttendance.forEach(attended => {
        const box = document.createElement('div');
        box.className = 'week-box' + (attended ? ' attended' : '');
        grid.appendChild(box);
    });
}

// 3-2. 주간 라벨(기간/이번 주 표시) 렌더링 (누락되어 있던 함수 - 추가)
function renderWeekLabel(weekLabel, isCurrentWeek) {
    const rangeEl = document.getElementById('weeklyRange');
    if (rangeEl) rangeEl.textContent = weekLabel;

    const titleEl = document.querySelector('.weekly-title');
    if (titleEl) titleEl.textContent = isCurrentWeek ? '이번 주 출석' : '지난 주 출석';
}

// 4. 새로고침(페이지네이션) 후 원래 있던 위치로 스크롤 복원
document.addEventListener('DOMContentLoaded', function () {
    const params = new URLSearchParams(window.location.search);
    const tab = params.get('tab');
    const subtab = params.get('subtab');

    if (tab) {
        const menuBtn = document.querySelector(`.menu-item[data-tab="${tab}"]`);
        if (menuBtn) {
            document.querySelectorAll('.menu-item').forEach(m => m.classList.remove('active'));
            menuBtn.classList.add('active');
        }

        if (subtab) {
            document.querySelectorAll('.subtab-item').forEach(s => s.classList.remove('active'));
            document.querySelectorAll('.subtab-panel').forEach(p => p.classList.remove('active'));
            const subBtn = document.querySelector(`.subtab-item[data-subtab="${subtab}"]`);
            const subPanel = document.getElementById('subtab-' + subtab);
            if (subBtn) subBtn.classList.add('active');
            if (subPanel) subPanel.classList.add('active');
        }

        const target = document.getElementById('panel-' + tab);
        if (target) {
            target.scrollIntoView({ behavior: 'auto', block: 'start' });
        }
    }
});
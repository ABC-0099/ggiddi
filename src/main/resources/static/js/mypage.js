document.addEventListener('DOMContentLoaded', function () {
    // 1. 기존 메뉴 클릭 이벤트 로직
    const menuItems = document.querySelectorAll('.menu-item');
    const subtabItems = document.querySelectorAll('.subtab-item');
    const subtabPanels = document.querySelectorAll('.subtab-panel');

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
            if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    });

    subtabItems.forEach(item => {
        item.addEventListener('click', function () {
            activateSubtab(this.getAttribute('data-subtab'));
        });
    });

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
document.addEventListener('DOMContentLoaded', function () {
    // 1. 기존 메뉴 클릭 이벤트 로직
    const menuItems = document.querySelectorAll('.menu-item');
    const subtabItems = document.querySelectorAll('.subtab-item');
    const subtabPanels = document.querySelectorAll('.subtab-panel');

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

    const contentPanels = document.querySelectorAll('.content-panel[id^="panel-"]');
    if (contentPanels.length > 0 && 'IntersectionObserver' in window) {
        const spy = new IntersectionObserver((entries) => {
            if (isProgrammaticScroll) return;
            const visible = entries.filter(e => e.isIntersecting);
            if (visible.length === 0) return;
            visible.sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top);
            const topId = visible[0].target.id.replace('panel-', '');
            activateMenu(topId);
        }, {
            root: null,
            rootMargin: '-96px 0px -60% 0px',
            threshold: 0
        });
        contentPanels.forEach(panel => spy.observe(panel));
    }

    // 2. 출석 달력 및 주간 출석 라벨 초기화
    window.currentViewDate = new Date();
    fetchAndRenderHeatmap(window.currentViewDate.getFullYear(), window.currentViewDate.getMonth());

    // [초기 로드 시 이번 주 날짜 범위 출력]
    const now = new Date();
    const dayOfWeek = now.getDay();
    const monday = new Date(now);
    monday.setDate(now.getDate() - (dayOfWeek === 0 ? 6 : dayOfWeek - 1));
    const sunday = new Date(monday);
    sunday.setDate(monday.getDate() + 6);
    const label = `${monday.getMonth() + 1}.${monday.getDate()} - ${sunday.getMonth() + 1}.${sunday.getDate()}`;
    renderWeekLabel(label, true);

    // 4. 새로고침 후 위치 복원
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
        if (target) target.scrollIntoView({ behavior: 'auto', block: 'start' });
    }
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
        const currentDate = new Date(year, month, i);
        if (currentDate.getDay() === 0) dayBox.classList.add('sun');
        if (currentDate.getDay() === 6) dayBox.classList.add('sat');
        const today = new Date();
        if (today.getFullYear() === year && today.getMonth() === month && today.getDate() === i) dayBox.classList.add('today');
        const dateStr = `${year}-${String(month + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
        if (attendanceSet.has(dateStr)) dayBox.classList.add('active');
        grid.appendChild(dayBox);
    }
}

window.changeMonth = function(delta) {
    if (typeof window.currentViewDate === 'undefined') window.currentViewDate = new Date();
    window.currentViewDate.setMonth(window.currentViewDate.getMonth() + delta);
    fetchAndRenderHeatmap(window.currentViewDate.getFullYear(), window.currentViewDate.getMonth());
};

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

function renderWeekLabel(weekLabel, isCurrentWeek) {
    const rangeEl = document.getElementById('weeklyRange');
    if (rangeEl) rangeEl.textContent = weekLabel;
    const titleEl = document.querySelector('.weekly-title');
    if (titleEl) titleEl.textContent = isCurrentWeek ? '이번 주 출석' : '지난 주 출석';
}
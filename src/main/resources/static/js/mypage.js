document.addEventListener('DOMContentLoaded', function () {
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

    // ★ URL 쿼리파라미터로 페이지네이션 클릭 후에도 탭 유지
    const params = new URLSearchParams(window.location.search);
    const tab = params.get('tab');
    const subtab = params.get('subtab');

    if (tab) {
        activateMenu(tab);
        if (subtab) activateSubtab(subtab);
        const target = document.getElementById('panel-' + tab);
        if (target) target.scrollIntoView({ behavior: 'auto', block: 'start' });
    }

    <script th:inline="javascript">
        window.onload = function() {
            var rawData = /*[[${heatmapData}]]*/ [];
            var grid = document.getElementById('calendarGrid');

            // 데이터 정렬 (날짜별 확인용)
            var attendanceMap = new Set(rawData.map(r => r.date));

            // 30일치 날짜 생성 (캘린더 형태)
            for (let i = 1; i <= 30; i++) {
                let dayBox = document.createElement('div');
                dayBox.style.aspectRatio = "1 / 1";
                dayBox.style.borderRadius = "4px";
                dayBox.style.display = "flex";
                dayBox.style.alignItems = "center";
                dayBox.style.justifyContent = "center";
                dayBox.style.fontSize = "10px";

                // 오늘 날짜와 비교하여 출석 여부 판단 (여기선 예시로 처리)
                let dateStr = "2026-07-" + (i < 10 ? '0' + i : i);
                if(attendanceMap.has(dateStr)) {
                    dayBox.style.backgroundColor = "#00E396"; // 출석 시 초록색
                    dayBox.style.color = "white";
                } else {
                    dayBox.style.backgroundColor = "#f3f4f6"; // 미출석 시 회색
                }
                dayBox.innerText = i;
                grid.appendChild(dayBox);
            }
        };
    </script>
});
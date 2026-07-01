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
});
document.addEventListener('DOMContentLoaded', function () {
    const menuItems = document.querySelectorAll('.menu-item');

    menuItems.forEach(item => {
        item.addEventListener('click', function () {
            const tab = this.getAttribute('data-tab');

            menuItems.forEach(m => m.classList.remove('active'));
            this.classList.add('active');

            const target = document.getElementById('panel-' + tab);
            if (target) {
                target.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }
        });
    });

    // 내 게시글 서브탭 전환
    const subtabItems = document.querySelectorAll('.subtab-item');
    const subtabPanels = document.querySelectorAll('.subtab-panel');

    subtabItems.forEach(item => {
        item.addEventListener('click', function () {
            const sub = this.getAttribute('data-subtab');

            subtabItems.forEach(s => s.classList.remove('active'));
            this.classList.add('active');

            subtabPanels.forEach(p => p.classList.remove('active'));
            document.getElementById('subtab-' + sub).classList.add('active');
        });
    });
});

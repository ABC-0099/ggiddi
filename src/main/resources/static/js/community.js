document.addEventListener('DOMContentLoaded', function() {
    const categoryButtons = document.querySelectorAll('.category-btn');
    const sortSelect = document.getElementById('sort-select');
    const btn_search = document.getElementById("btn_search");
    const search_input = document.getElementById("search_kw");

    // URL 파라미터 가져오기
    const urlParams = new URLSearchParams(window.location.search);
    const currentCategory = urlParams.get('category') || 'ALL';
    const currentSort = urlParams.get('sort') || 'newest';
    const currentKw = urlParams.get('kw') || '';

    // 검색어와 정렬 상태 UI 반영
    if (search_input) search_input.value = currentKw;
    if (sortSelect) sortSelect.value = currentSort;

    // 카테고리 클릭 시 이동
    categoryButtons.forEach(button => {
        if (button.getAttribute('data-category') === currentCategory) {
            button.classList.add('active');
        }
        button.addEventListener('click', function() {
            const cat = this.getAttribute('data-category');
            movePage(cat, currentSort, currentKw);
        });
    });

    // 정렬 변경 시 이동
    sortSelect.addEventListener('change', function() {
        movePage(currentCategory, this.value, currentKw);
    });

    // 검색 버튼 클릭
    if (btn_search) {
        btn_search.addEventListener('click', function() {
            movePage(currentCategory, currentSort, search_input.value);
        });
    }

    // 엔터키 검색
    if (search_input) {
        search_input.addEventListener("keydown", (e) => {
            if (e.key === "Enter") btn_search.click();
        });
    }

    // 페이지 이동 공통 함수
    function movePage(cat, sort, kw) {
        let url = `/community?sort=${sort}&kw=${encodeURIComponent(kw)}`;
        if (cat !== 'ALL') url += `&category=${encodeURIComponent(cat)}`;
        location.href = url;
    }
});
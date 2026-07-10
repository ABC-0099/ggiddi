/**
 * ===== 끼역이 / 띠귿이 플로팅 버튼 드래그 이동 =====
 *
 * - 끼역이(#kkFab)는 community/list.html 에서 사용
 * - 띠귿이(#ddigeudToggleBtn)는 content/view.html 에서 사용
 * - 두 버튼 모두 이 스크립트 하나로 처리 가능 (페이지에 해당 id가 없으면 자동 스킵)
 * - 마우스 + 터치 지원
 * - 드래그 후에는 클릭 이벤트를 막아서 toggleKk() / ddigeudToggleBtn 클릭 핸들러가
 *   실수로 패널을 열고 닫지 않게 함
 * - 위치는 localStorage에 저장 (버튼별로 key 다르게 저장됨)
 */

(function () {
  function makeDraggable(elementId, storageKey) {
    const el = document.getElementById(elementId);
    if (!el) return; // 이 페이지에 해당 버튼이 없으면 조용히 스킵

    let isDragging = false;
    let hasMoved = false;
    let startX, startY, initialLeft, initialTop;

    // 저장된 위치 복원
    const saved = localStorage.getItem(storageKey);
    if (saved) {
      try {
        const { left, top } = JSON.parse(saved);
        el.style.left = left + "px";
        el.style.top = top + "px";
        el.style.right = "auto";
        el.style.bottom = "auto";
      } catch (e) { /* 저장값 손상 시 무시하고 기본 위치 사용 */ }
    }

    // 기존 CSS가 position:fixed가 아니면 강제 지정
    const computedPos = window.getComputedStyle(el).position;
    if (computedPos !== "fixed") {
      el.style.position = "fixed";
    }

    function getPoint(e) {
      if (e.touches && e.touches.length > 0) {
        return { x: e.touches[0].clientX, y: e.touches[0].clientY };
      }
      return { x: e.clientX, y: e.clientY };
    }

    function onStart(e) {
      isDragging = true;
      hasMoved = false;
      const point = getPoint(e);
      startX = point.x;
      startY = point.y;

      const rect = el.getBoundingClientRect();
      initialLeft = rect.left;
      initialTop = rect.top;

      el.style.transition = "none";
    }

    function onMove(e) {
      if (!isDragging) return;

      const point = getPoint(e);
      const dx = point.x - startX;
      const dy = point.y - startY;

      if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
        hasMoved = true;
      }

      if (hasMoved) {
        if (e.cancelable) e.preventDefault(); // 터치 시 스크롤 방지

        let newLeft = initialLeft + dx;
        let newTop = initialTop + dy;

        const maxLeft = window.innerWidth - el.offsetWidth;
        const maxTop = window.innerHeight - el.offsetHeight;
        newLeft = Math.max(0, Math.min(newLeft, maxLeft));
        newTop = Math.max(0, Math.min(newTop, maxTop));

        el.style.left = newLeft + "px";
        el.style.top = newTop + "px";
        el.style.right = "auto";
        el.style.bottom = "auto";
      }
    }

    function onEnd() {
      if (!isDragging) return;
      isDragging = false;
      el.style.transition = "";

      if (hasMoved) {
        const rect = el.getBoundingClientRect();
        localStorage.setItem(
          storageKey,
          JSON.stringify({ left: rect.left, top: rect.top })
        );
      }
    }

    // 마우스
    el.addEventListener("mousedown", onStart);
    document.addEventListener("mousemove", onMove);
    document.addEventListener("mouseup", onEnd);

    // 터치
    el.addEventListener("touchstart", onStart, { passive: true });
    document.addEventListener("touchmove", onMove, { passive: false });
    document.addEventListener("touchend", onEnd);

    // 드래그 직후 클릭(= toggleKk 실행, ddigeudToggleBtn 리스너 실행)을 막기
    // capture 단계에서 가로채서 inline onclick, addEventListener 클릭 모두 차단
    el.addEventListener(
      "click",
      function (e) {
        if (hasMoved) {
          e.stopImmediatePropagation();
          e.preventDefault();
          hasMoved = false; // 다음 클릭은 정상 동작하도록 리셋
        }
      },
      true // capture: true → toggleKk()/addEventListener보다 먼저 실행됨
    );
  }

  document.addEventListener("DOMContentLoaded", function () {
    makeDraggable("kkFab", "kkFab-position");
    makeDraggable("ddigeudToggleBtn", "ddigeudToggleBtn-position");
  });
})();
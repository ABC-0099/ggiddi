// content.html 전용 스크립트 - 콘텐츠 등록/수정 모달 제어

function openContentModal() {
    document.getElementById('contentCreateModal').classList.add('open');
}
function closeContentModal() {
    document.getElementById('contentCreateModal').classList.remove('open');
}

function openContentUpdateModal(id, title, step, lectureCount, status) {
    document.getElementById('update-title').value = title;
    document.getElementById('update-step').value = step;
    document.getElementById('update-lectureCount').value = lectureCount;
    document.getElementById('update-status').value = status;

    document.getElementById('contentUpdateForm').action = '/admin/content/update/' + id;

    document.getElementById('contentUpdateModal').classList.add('open');
}
function closeContentUpdateModal() {
    document.getElementById('contentUpdateModal').classList.remove('open');
}

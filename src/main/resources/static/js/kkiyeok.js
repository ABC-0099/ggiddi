/* ===== 끼역이 AI 채팅봇 ===== */
const kkPanel = document.getElementById('kkPanel');
const kkBody = document.getElementById('kkBody');
const kkInput = document.getElementById('kkInput');
const kkTyping = document.getElementById('kkTyping');
const kkQuickEl = document.getElementById('kkQuick');
const kkCsrf = document.querySelector('meta[name="_csrf"]')?.content;
const kkCsrfH = document.querySelector('meta[name="_csrf_header"]')?.content;

document.getElementById('kkFirstTime').textContent = kkTime();
kkInput.addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.isComposing) { e.preventDefault(); kkSend(); }
});

function toggleKk() {
    const open = kkPanel.classList.toggle('open');
    document.getElementById('kkFab').classList.toggle('active', open);
    if (open) kkInput.focus();
}

function kkTime() {
    const n = new Date();
    return n.getHours().toString().padStart(2, '0') + ':' + n.getMinutes().toString().padStart(2, '0');
}

function kkEsc(s) {
    const d = document.createElement('div');
    d.textContent = s;
    return d.innerHTML;
}

function kkAdd(text, isUser) {
    const div = document.createElement('div');
    div.className = `kk-row ${isUser ? 'user' : 'bot'}`;
    if (isUser) {
        div.innerHTML = `<div><div class="kk-bubble">${kkEsc(text)}</div><div class="kk-time">${kkTime()}</div></div>`;
    } else {
        div.innerHTML = `<div class="kk-row-ava">🐸</div><div><div class="kk-bubble">${kkEsc(text).replace(/\n/g, '<br>')}</div><div class="kk-time">${kkTime()}</div></div>`;
    }
    kkBody.appendChild(div);
    kkBody.scrollTop = kkBody.scrollHeight;
}

async function kkSend() {
    const text = kkInput.value.trim();
    if (!text) return;

    const w = document.querySelector('.kk-welcome');
    if (w) w.style.display = 'none';
    kkQuickEl.style.display = 'none';

    kkAdd(text, true);
    kkInput.value = '';
    document.getElementById('kkSendBtn').disabled = true;
    kkTyping.classList.add('show');
    kkBody.scrollTop = kkBody.scrollHeight;

    try {
        const h = { 'Content-Type': 'application/json' };
        if (kkCsrfH && kkCsrf) h[kkCsrfH] = kkCsrf;

        const res = await fetch('/api/ai-chat', {
            method: 'POST',
            headers: h,
            body: JSON.stringify({ message: text })
        });
        const data = await res.json();
        kkTyping.classList.remove('show');
        kkAdd(data.reply || '음... 뭔가 이상해 😅', false);
    } catch (err) {
        kkTyping.classList.remove('show');
        kkAdd('앗, 연결이 안 돼! 다시 해볼래? 🌐', false);
    }

    document.getElementById('kkSendBtn').disabled = false;
    kkInput.focus();
}

function kkQ(t) {
    kkInput.value = t;
    kkSend();
}
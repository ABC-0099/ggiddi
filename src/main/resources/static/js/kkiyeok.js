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
        div.innerHTML = `<div class="kk-row-ava">🐸</div><div>
            <div class="kk-bubble">${kkEsc(text).replace(/\n/g, '<br>')}</div>
            <div class="kk-time">${kkTime()}</div>
            <div class="kk-translate-wrap">
                <button type="button" class="kk-translate-toggle" onclick="kkToggleTranslateMenu(event, this)">🌐 번역</button>
                <div class="kk-translate-menu">
                    <button type="button" onclick="kkRequestTranslate(event, this, 'en')">English</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'zh')">中文</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'ja')">日本語</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'vi')">Tiếng Việt</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'tl')">Filipino</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'id')">Bahasa Indonesia</button>
                    <button type="button" onclick="kkRequestTranslate(event, this, 'th')">ไทย</button>
                </div>
            </div>
            <div class="kk-translated" style="display:none;"></div>
        </div>`;
        div.dataset.rawText = text; // 번역 요청 시 원문 그대로 사용 (이스케이프 전 텍스트)
    }
    kkBody.appendChild(div);
    kkBody.scrollTop = kkBody.scrollHeight;
}

// ── 끼역이 답변 번역 ──
function kkCloseAllTranslateMenus(exceptMenu) {
    document.querySelectorAll('.kk-translate-menu.open').forEach(menu => {
        if (menu !== exceptMenu) menu.classList.remove('open');
    });
}

function kkToggleTranslateMenu(evt, btnEl) {
    evt.stopPropagation();
    const menu = btnEl.nextElementSibling;
    const isOpen = menu.classList.contains('open');
    kkCloseAllTranslateMenus(menu);
    menu.classList.toggle('open', !isOpen);
}

document.addEventListener('click', () => kkCloseAllTranslateMenus(null));

function kkRequestTranslate(evt, btnEl, targetLang) {
    evt.stopPropagation();

    const row = btnEl.closest('.kk-row');
    const text = row.dataset.rawText || '';
    const translatedBox = row.querySelector('.kk-translated');
    const menu = btnEl.closest('.kk-translate-menu');

    translatedBox.style.display = 'block';
    translatedBox.textContent = '번역 중…';
    menu.classList.remove('open');

    const h = { 'Content-Type': 'application/json' };
    if (kkCsrfH && kkCsrf) h[kkCsrfH] = kkCsrf;

    fetch('/api/translate', {
        method: 'POST',
        headers: h,
        body: JSON.stringify({ text: text, targetLang: targetLang })
    })
        .then(res => res.json())
        .then(data => {
            translatedBox.textContent = data.translatedText;
        })
        .catch(() => {
            translatedBox.textContent = '번역에 실패했어요.';
        });
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
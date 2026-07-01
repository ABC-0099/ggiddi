document.addEventListener('DOMContentLoaded', function () {
    const chatPanel = document.getElementById('chat-panel');
    const chatToggleBtn = document.getElementById('chat-toggle-btn');
    const unreadBadge = document.getElementById('chat-unread-badge');

    // 비로그인 사용자는 채팅 위젯 자체가 렌더링되지 않으므로 여기서 종료
    if (!chatToggleBtn || !chatPanel) return;

    const viewRooms = document.getElementById('chat-view-rooms');
    const viewRoom = document.getElementById('chat-view-room');
    const roomListEl = document.getElementById('chat-room-list');
    const roomNameEl = document.getElementById('chat-room-name');

    const chatMessages = document.getElementById('chat-messages');
    const chatEmpty = document.getElementById('chat-empty');
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const chatSendBtn = document.getElementById('chat-send-btn');
    const statusDot = document.getElementById('chat-status-dot');

    const currentUser = document.body.getAttribute('data-current-user') || '';

    let stompClient = null;
    let isPanelOpen = false;
    let unreadCount = 0;
    let isConnected = false;
    let currentRoomId = null;
    let currentSubscription = null;

    // ───────── 패널 열기/닫기 ─────────
    window.toggleChat = function () {
        isPanelOpen = !isPanelOpen;
        chatPanel.classList.toggle('open', isPanelOpen);
        if (isPanelOpen) {
            unreadCount = 0;
            updateUnreadBadge();
            if (currentRoomId) {
                scrollToBottom();
                chatInput.focus();
            } else {
                loadRoomList();
            }
        }
    };

    // ───────── 로비 목록 화면 ─────────
    function loadRoomList() {
        roomListEl.innerHTML = '<div class="chat-room-loading">로비 목록을 불러오는 중...</div>';

        fetch('/api/chat/rooms')
            .then(res => res.json())
            .then(rooms => renderRoomList(rooms))
            .catch(err => {
                console.error('로비 목록 로드 실패', err);
                roomListEl.innerHTML = '<div class="chat-room-loading">로비 목록을 불러오지 못했어요.</div>';
            });
    }

    function renderRoomList(rooms) {
        roomListEl.innerHTML = '';

        rooms.forEach(room => {
            const card = document.createElement('div');
            card.className = 'quest-chat-room-card' + (room.full ? ' is-full' : '');

            const icon = document.createElement('div');
            icon.className = 'quest-chat-room-icon';
            icon.textContent = '🔥';

            const info = document.createElement('div');
            info.className = 'quest-chat-room-info';

            const title = document.createElement('div');
            title.className = 'quest-chat-room-title';
            title.textContent = room.name;

            const sub = document.createElement('div');
            sub.className = 'quest-chat-room-sub';
            sub.textContent = room.full ? '야영지가 가득 찼어요' : '모닥불에 둘러앉아 대화해보세요';

            info.appendChild(title);
            info.appendChild(sub);

            const count = document.createElement('div');
            count.className = 'quest-chat-room-count' + (room.full ? ' is-full' : '');
            count.textContent = room.currentCount + ' / ' + room.capacity;

            card.appendChild(icon);
            card.appendChild(info);
            card.appendChild(count);

            if (!room.full) {
                card.addEventListener('click', () => enterRoom(room.id, room.name));
            }

            roomListEl.appendChild(card);
        });
    }

    // ───────── 방 입장/퇴장 ─────────
    function enterRoom(roomId, roomName) {
        currentRoomId = roomId;
        roomNameEl.textContent = roomName;

        chatMessages.innerHTML = '';
        const emptyEl = document.createElement('div');
        emptyEl.className = 'quest-chat-empty';
        emptyEl.id = 'chat-empty';
        emptyEl.textContent = '아직 모닥불 앞에 아무도 없어요. 먼저 말을 걸어보세요 🔥';
        chatMessages.appendChild(emptyEl);

        viewRooms.style.display = 'none';
        viewRoom.style.display = 'flex';

        connect(roomId);
        chatInput.focus();
    }

    window.leaveRoom = function () {
        disconnectRoom();
        currentRoomId = null;
        viewRoom.style.display = 'none';
        viewRooms.style.display = 'flex';
        loadRoomList(); // 인원수 갱신을 위해 다시 로드
    };

    function disconnectRoom() {
        if (currentSubscription) {
            currentSubscription.unsubscribe();
            currentSubscription = null;
        }
        if (stompClient) {
            stompClient.deactivate();
            stompClient = null;
        }
        setConnected(false);
    }

    // ───────── 연결 상태 UI 반영 ─────────
    function setConnected(connected) {
        isConnected = connected;
        if (statusDot) {
            statusDot.classList.toggle('online', connected);
        }
        if (chatSendBtn) {
            chatSendBtn.disabled = !connected;
        }
        if (chatInput) {
            chatInput.placeholder = connected ? '메시지를 입력하세요' : '연결 중...';
        }
    }

    // ───────── 메시지 렌더링 ─────────
    function appendMessage(msg) {
        const emptyEl = document.getElementById('chat-empty');
        if (emptyEl) emptyEl.style.display = 'none';

        const isMine = currentUser && msg.username === currentUser;

        const wrap = document.createElement('div');
        wrap.className = 'quest-chat-msg ' + (isMine ? 'is-mine' : 'is-other');

        const authorEl = document.createElement('div');
        authorEl.className = 'quest-chat-msg-author';
        authorEl.textContent = isMine ? '나' : msg.username;
        wrap.appendChild(authorEl);

        const row = document.createElement('div');
        row.className = 'quest-chat-bubble-row';

        const bubble = document.createElement('div');
        bubble.className = 'quest-chat-bubble';
        bubble.textContent = msg.content;

        const time = document.createElement('div');
        time.className = 'quest-chat-msg-time';
        time.textContent = msg.time;

        row.appendChild(bubble);
        row.appendChild(time);
        wrap.appendChild(row);

        chatMessages.appendChild(wrap);
        scrollToBottom();

        const isViewingThisRoom = isPanelOpen && currentRoomId === msg.roomId;
        if (!isViewingThisRoom && !isMine) {
            unreadCount++;
            updateUnreadBadge();
        }
    }

    function scrollToBottom() {
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function updateUnreadBadge() {
        if (unreadCount > 0) {
            unreadBadge.style.display = 'flex';
            unreadBadge.textContent = unreadCount > 99 ? '99+' : unreadCount;
        } else {
            unreadBadge.style.display = 'none';
        }
    }

    // ───────── 이전 대화 내역 로드 ─────────
    function loadHistory(roomId) {
        fetch('/api/chat/rooms/' + roomId + '/history?limit=50')
            .then(res => res.json())
            .then(list => {
                list.forEach(appendMessage);
            })
            .catch(err => console.error('채팅 내역 로드 실패', err));
    }

    // ───────── WebSocket(STOMP) 연결 ─────────
    function connect(roomId) {
        const socket = new SockJS('/ws/chat');
        stompClient = new StompJs.Client({
            webSocketFactory: () => socket,
            reconnectDelay: 3000,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,

            onConnect: () => {
                setConnected(true);
                currentSubscription = stompClient.subscribe('/topic/chat/' + roomId, (frame) => {
                    const body = JSON.parse(frame.body);
                    appendMessage(body);
                });
                loadHistory(roomId);
            },

            onDisconnect: () => {
                setConnected(false);
            },

            onWebSocketClose: () => {
                setConnected(false);
            },

            onWebSocketError: () => {
                setConnected(false);
            },

            onStompError: (frame) => {
                console.error('STOMP 오류', frame.headers && frame.headers['message']);
                setConnected(false);
            },
        });

        stompClient.activate();
    }

    // ───────── 메시지 전송 ─────────
    function sendMessage() {
        const content = chatInput.value.trim();
        if (!content || !currentRoomId) return;

        if (!stompClient || !isConnected) {
            console.warn('채팅 서버와 연결이 끊겨있어요. 재연결을 시도합니다.');
            setConnected(false);
            if (!stompClient) connect(currentRoomId);
            return;
        }

        try {
            stompClient.publish({
                destination: '/app/chat/' + currentRoomId + '/send',
                body: JSON.stringify({ content }),
            });
            chatInput.value = '';
        } catch (err) {
            console.error('메시지 전송 실패', err);
            setConnected(false);
        }
    }

    chatForm.addEventListener('submit', function (e) {
        e.preventDefault();
        sendMessage();
    });

    chatInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    setConnected(false);
});
// --- KONFIGURASI ---
// Sesuaikan dengan port backend Spring Boot Anda
const BASE_URL = "http://localhost:8011";
const BACKEND_API_URL = "http://localhost:8080";

// --- STATE GLOBAL ---
let socket = null;
let myUser = { id: null, name: null };
let targetUser = { id: null, name: null };

// --- FUNGSI NAVIGASI HALAMAN ---
function showPage(pageId) {
    ['page-login', 'page-userlist', 'page-chat'].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.classList.add('hidden');
            el.classList.remove('flex');
        }
    });
    const page = document.getElementById(pageId);
    if (page) {
        page.classList.remove('hidden');
        page.classList.add('flex');
    }
}

// --- 1. LOGIKA LOGIN & SOCKET ---
function doLogin() {
    const idVal = document.getElementById('input-userid').value.trim();
    const nameVal = document.getElementById('input-username').value.trim();

    if (!idVal || !nameVal) {
        alert("Mohon isi User ID dan Username");
        return;
    }

    myUser.id = idVal;
    myUser.name = nameVal;

    // Update UI Profil Saya
    document.getElementById('my-display-name').innerText = myUser.name;
    document.getElementById('my-avatar-initial').innerText = myUser.name.charAt(0).toUpperCase();

    // Inisialisasi Koneksi Socket
    connectSocket(myUser.id, myUser.name);
}

function connectSocket(userId, userName) {
    const errorDiv = document.getElementById('login-error');
    if(errorDiv) errorDiv.classList.add('hidden');

    // Sesuai logika backend: query params 'userId' dan 'userName'
    // Pastikan socket.io.js sudah di-load di HTML sebelum script ini jalan
    if (typeof io === 'undefined') {
        alert("Library Socket.IO tidak terdeteksi!");
        return;
    }

    socket = io(BASE_URL, {
        query: {
            userId: userId,
            userName: userName
        },
        transports: ['polling', 'websocket']
    });

    socket.on('connect', () => {
        console.log("Socket Connected:", socket.id);
        // Setelah konek sukses, pindah ke halaman list user
        fetchUsers();
        showPage('page-userlist');
    });

    socket.on('connect_error', (err) => {
        console.error("Socket Error:", err);
        if(errorDiv) {
            errorDiv.innerText = "Gagal terhubung ke server: " + err.message;
            errorDiv.classList.remove('hidden');
        }
    });

    // LISTENER PESAN MASUK
    socket.on('get_message', (data) => {
        console.log("Pesan diterima:", data);
        handleIncomingMessage(data);
    });
}

// --- 2. LOGIKA GET ALL USERS ---
async function fetchUsers() {
    const loading = document.getElementById('loading-users');
    const container = document.getElementById('user-list-container');

    if(loading) loading.classList.remove('hidden');
    if(container) {
        container.classList.add('hidden');
        container.innerHTML = ''; // Bersihkan list lama
    }

    try {
        // Fetch ke endpoint Controller
        const response = await fetch(`${BACKEND_API_URL}/api/users`);
        if(!response.ok) throw new Error("Gagal mengambil data user");

        const users = await response.json();

        // Render List (Kecuali diri sendiri)
        users.forEach(user => {
            if (String(user.id) !== String(myUser.id)) {
                const el = document.createElement('div');
                el.className = "bg-white p-4 rounded-xl border border-slate-200 shadow-sm hover:shadow-md transition cursor-pointer flex items-center gap-4 active:scale-[0.99]";
                el.onclick = () => openChat(user);

                el.innerHTML = `
                        <div class="w-12 h-12 rounded-full bg-slate-200 flex items-center justify-center text-slate-600 font-bold text-lg">
                            ${user.username ? user.username.charAt(0).toUpperCase() : '?'}
                        </div>
                        <div class="flex-1">
                            <h4 class="font-bold text-slate-800">${user.username}</h4>
                            <p class="text-xs text-slate-500">ID: ${user.id}</p>
                        </div>
                        <div class="text-slate-300">
                            <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                            </svg>
                        </div>
                    `;
                container.appendChild(el);
            }
        });

        if(container.children.length === 0) {
            container.innerHTML = `<div class="text-center py-4 text-slate-400 text-sm">Belum ada user lain yang terdaftar.</div>`;
        }

    } catch (e) {
        console.error(e);
        // alert("Gagal memuat daftar user. Pastikan backend berjalan.");
    } finally {
        if(loading) loading.classList.add('hidden');
        if(container) container.classList.remove('hidden');
    }
}

// --- 3. LOGIKA CHAT ---

// Fungsi baru: Ambil History dari DB
async function fetchChatHistory(targetId) {
    const container = document.getElementById('messages-area');
    if(!container) return;

    container.innerHTML = '<div class="text-center text-xs text-slate-400 my-4">Memuat percakapan...</div>';

    try {
        // Panggil endpoint API baru
        const res = await fetch(`${BACKEND_API_URL}/api/chats/${myUser.id}/${targetId}`);
        if(!res.ok) throw new Error("Failed to fetch history");

        const messages = await res.json();
        container.innerHTML = ''; // Clear loading msg

        if(messages.length === 0) {
            container.innerHTML = '<div class="text-center text-xs text-slate-400 my-4">-- Mulai percakapan --</div>';
        }

        messages.forEach(msg => {
            // Tentukan apakah pesan ini 'send' (dari saya) atau 'receive' (dari lawan)
            // Backend mengirim senderId sebagai Integer
            const isSender = String(msg.senderId) === String(myUser.id);

            appendMessageToUI({
                text: msg.content,
                type: isSender ? 'send' : 'receive',
                time: msg.createdAt // Format Timestamp dari Java
            });
        });

        scrollToBottom();

    } catch (e) {
        console.error("Error fetching history:", e);
        container.innerHTML = '<div class="text-center text-xs text-red-400 my-4">Gagal memuat history</div>';
    }
}

function openChat(user) {
    targetUser = { id: String(user.id), name: user.username };

    // Update UI Header Chat
    document.getElementById('chat-target-name').innerText = targetUser.name;
    document.getElementById('chat-target-id').innerText = "ID: " + targetUser.id;
    document.getElementById('chat-target-avatar').innerText = targetUser.name.charAt(0).toUpperCase();

    showPage('page-chat');

    // Panggil history dari DB
    fetchChatHistory(targetUser.id);
}

function backToUserList() {
    targetUser = { id: null, name: null };
    showPage('page-userlist');
}

function sendMessage() {
    const input = document.getElementById('input-message');
    const text = input.value.trim();

    if (!text || !socket || !targetUser.id) return;

    // Payload sesuai ReqSendMessageDto di backend
    const payload = {
        type: "CLIENT",
        message: text,
        userTargetId: targetUser.id
    };

    // Emit ke Backend
    socket.emit('send_message', payload);

    // Tampilkan langsung di UI (Optimistic Update)
    appendMessageToUI({
        text: text,
        type: 'send',
        time: new Date()
    });

    input.value = '';
    scrollToBottom();
}

function handleIncomingMessage(data) {
    const senderId = String(data.senderId);

    // Jika pesan dari user yang sedang kita chat sekarang, tampilkan
    const pageChat = document.getElementById('page-chat');
    if (targetUser.id === senderId && pageChat && !pageChat.classList.contains('hidden')) {
        appendMessageToUI({
            text: data.message,
            type: 'receive',
            time: new Date() // Gunakan waktu sekarang karena realtime
        });
        scrollToBottom();
    } else {
        // Jika sedang di menu list user atau chat dengan orang lain
        console.log(`Notifikasi: Pesan baru dari ${data.senderName}`);
    }
}

function appendMessageToUI(msg) {
    const container = document.getElementById('messages-area');
    if(!container) return;

    const isSend = msg.type === 'send';

    const div = document.createElement('div');
    div.className = `flex w-full ${isSend ? 'justify-end' : 'justify-start'} fade-in`;

    // Handle format waktu (bisa dari String Java Timestamp atau JS Date Object)
    let timeStr = "";
    try {
        const dateObj = new Date(msg.time);
        timeStr = dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    } catch(e) {
        timeStr = "--:--";
    }

    div.innerHTML = `
            <div class="message-bubble px-4 py-2 rounded-2xl shadow-sm text-sm ${
        isSend
            ? 'bg-blue-600 text-white rounded-br-none'
            : 'bg-white text-slate-700 border border-slate-200 rounded-bl-none'
    }">
                <p>${escapeHtml(msg.text)}</p>
                <span class="text-[10px] block mt-1 text-right opacity-70">${timeStr}</span>
            </div>
        `;
    container.appendChild(div);
}

function scrollToBottom() {
    const container = document.getElementById('messages-area');
    if(container) {
        setTimeout(() => {
            container.scrollTop = container.scrollHeight;
        }, 50);
    }
}

function logout() {
    if(socket) socket.disconnect();
    myUser = {};
    targetUser = {};
    location.reload();
}

function escapeHtml(text) {
    if (!text) return text;
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}
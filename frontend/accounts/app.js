const API_URL = 'http://localhost:8080/api/transfers';
const AUTH_URL = 'http://localhost:8080/api/auth';
const ME_URL = 'http://localhost:8080/api/me/balance';

let successCount = 0;
let failCount = 0;
let jwtToken = '';

// Auto-login to get a JWT token and seed test users
async function autoLogin() {
    try {
        const urlParams = new URLSearchParams(window.location.search);
        const loginUser = urlParams.get('user') || 'alice';
        const credentials = { username: loginUser, password: "password" };
        
        const allUsers = [
            { username: "alice", password: "password" },
            { username: "bob", password: "password" },
            { username: "charlie", password: "password" },
            { username: "david", password: "password" }
        ];
        
        // Register All Users to ensure they exist in DB
        for (const user of allUsers) {
            await fetch(`${AUTH_URL}/register`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(user)
            });
        }

        const res = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(credentials)
        });
        
        if (res.ok) {
            const data = await res.json();
            jwtToken = data.token;
            console.log("Auto-login successful. Token acquired.");
            if (document.getElementById('currentUser')) {
                // Capitalize first letter for display name
                const displayName = credentials.username.charAt(0).toUpperCase() + credentials.username.slice(1);
                document.getElementById('currentUser').textContent = displayName;
            }
            if (document.getElementById('accountUsername')) {
                document.getElementById('accountUsername').textContent = credentials.username;
            }
            
            // Populate Dropdown dynamically
            const selectElement = document.getElementById('toUsername');
            selectElement.innerHTML = '<option value="" disabled selected>-- Select Recipient Account --</option>';
            for (const user of allUsers) {
                if (user.username !== credentials.username) {
                    const optionName = user.username.charAt(0).toUpperCase() + user.username.slice(1);
                    const option = document.createElement('option');
                    option.value = user.username;
                    option.textContent = `${optionName} (${user.username})`;
                    selectElement.appendChild(option);
                }
            }
            
            fetchBalance();
            connectWebSocket(credentials.username);
        }
    } catch (e) {
        console.error("Auto-login failed:", e);
    }
}

let stompClient = null;

function connectWebSocket(username) {
    const socket = new SockJS('http://localhost:8080/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = null; // Disable debug logs

    stompClient.connect({}, function (frame) {
        console.log('Connected to WebSocket for live updates: ' + frame);
        stompClient.subscribe('/topic/balance/' + username, function (message) {
            console.log("WebSocket Balance Update Received!");
            // Flash the balance area green to indicate a live update
            const balanceArea = document.getElementById('currentBalance').parentElement;
            balanceArea.style.color = '#22c55e'; // Green
            setTimeout(() => { balanceArea.style.color = ''; }, 1000);
            
            // Re-fetch the balance to get the actual new amount
            fetchBalance();
        });
    });
}

async function fetchBalance() {
    if(!jwtToken) return;
    try {
        const res = await fetch(ME_URL, {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (res.ok) {
            const balance = await res.text();
            document.getElementById('currentBalance').textContent = parseFloat(balance).toFixed(2);
            await fetchTransactionHistory();
        }
    } catch (e) {
        console.error("Could not fetch balance", e);
    }
}



async function fetchTransactionHistory() {
    if (!jwtToken) return;
    try {
        const res = await fetch('http://localhost:8080/api/transactions/my-history', {
            headers: { 'Authorization': `Bearer ${jwtToken}` }
        });
        if (res.ok) {
            const transactions = await res.json();
            const tbody = document.getElementById('transactionTableBody');
            tbody.innerHTML = '';
            
            if (transactions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" style="text-align: center; color: #64748b; padding: 2rem;">No recent transactions</td></tr>';
                return;
            }
            
            transactions.forEach(t => {
                const tr = document.createElement('tr');
                const date = new Date(t.createdAt).toLocaleString();
                
                let amountHtml = '';
                if (t.type === 'INCOME') {
                    amountHtml = `<span style="color: #22c55e; font-weight: 600;">+ $${t.amount.toFixed(2)}</span>`;
                } else {
                    amountHtml = `<span style="color: #ef4444; font-weight: 600;">- $${t.amount.toFixed(2)}</span>`;
                }
                
                const statusHtml = `<span class="azure-badge" style="background-color: #e0f2fe; color: #0284c7;">COMPLETED</span>`;
                
                tr.innerHTML = `
                    <td style="color: #64748b; font-size: 0.85rem;">${date}</td>
                    <td style="font-weight: 500;">${t.description}</td>
                    <td>${amountHtml}</td>
                    <td>${statusHtml}</td>
                `;
                tbody.appendChild(tr);
            });
        }
    } catch (e) {
        console.error("Could not fetch history", e);
    }
}

autoLogin();

document.getElementById('transferForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    
    if (!jwtToken) {
        addLog(`Auth Error: JWT Token not ready yet.`, 'error');
        return;
    }

    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.textContent = 'Processing...';

    const payload = {
        toUsername: document.getElementById('toUsername').value,
        amount: parseFloat(document.getElementById('amount').value),
        description: document.getElementById('description').value || 'Web Transfer'
    };

    try {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${jwtToken}`
            },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            successCount++;
            document.getElementById('successCount').textContent = successCount;
            addLog(`Transfer of $${payload.amount} to ${payload.toUsername} successful.`, 'success');
            document.getElementById('transferForm').reset();
            fetchBalance();
        } else {
            const errText = await response.text();
            failCount++;
            document.getElementById('failCount').textContent = failCount;
            addLog(`Transfer failed: ${errText || response.statusText}`, 'error');
        }
    } catch (error) {
        failCount++;
        document.getElementById('failCount').textContent = failCount;
        addLog(`Network Error: Ensure backend is running.`, 'error');
    } finally {
        btn.disabled = false;
        btn.textContent = 'Authorize Payment';
    }
});

function addLog(message, type) {
    const logList = document.getElementById('logList');
    const li = document.createElement('li');
    li.className = `log-item ${type}`;
    
    const timestamp = new Date().toLocaleTimeString();
    li.innerHTML = `<strong>[${timestamp}]</strong> ${message}`;
    
    logList.prepend(li);
}

// UI Race Condition Simulation (Fires 5 Requests at the EXACT Same Millisecond)
document.getElementById('attackBtn').addEventListener('click', async () => {
    if (!jwtToken) {
        addLog(`Auth Error: JWT Token not ready yet.`, 'error');
        return;
    }

    const recipient = document.getElementById('toUsername').value || 'bob';
    const amountVal = parseFloat(document.getElementById('amount').value) || 100.00;
    
    const payload = {
        toUsername: recipient,
        amount: amountVal,
        description: "5x Simultaneous Race Condition Request"
    };

    addLog(`FIRING 5 SIMULTANEOUS $${amountVal} REQUESTS TO ${recipient}...`, 'info');

    const makeRequest = () => fetch(API_URL, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${jwtToken}`
        },
        body: JSON.stringify(payload)
    });

    try {
        // Fire 5 HTTP requests concurrently at the EXACT same millisecond
        const promises = [makeRequest(), makeRequest(), makeRequest(), makeRequest(), makeRequest()];
        const responses = await Promise.all(promises);

        for (let i = 0; i < responses.length; i++) {
            const res = responses[i];
            const reqNum = i + 1;
            if (res.ok) {
                successCount++;
                document.getElementById('successCount').textContent = successCount;
                addLog(`[Req #${reqNum}] Transfer of $${payload.amount} to ${recipient} SUCCESSFUL (200 OK)`, 'success');
            } else {
                const errText = await res.text();
                failCount++;
                document.getElementById('failCount').textContent = failCount;
                addLog(`[Req #${reqNum}] Transfer BLOCKED: ${errText || res.statusText}`, 'error');
            }
        }

        fetchBalance();

    } catch (error) {
        addLog(`Network Error during race condition test`, 'error');
    }
});

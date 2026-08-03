const urlLogin = 'http://localhost:8080/api/auth/login';
const urlTransfer = 'http://localhost:8080/api/transfers';

const aliceCredentials = { username: "alice", password: "password" };

async function debug() {
    const loginRes = await fetch(urlLogin, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(aliceCredentials) });
    const { token } = await loginRes.json();

    const transferRes = await fetch(urlTransfer, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
        body: JSON.stringify({ fromUserId: 1, toUserId: 2, amount: 100.00, description: "Stress test transfer" })
    });
    console.log("Status:", transferRes.status);
    console.log("Body:", await transferRes.text());
}
debug();

const urlRegister = 'http://localhost:8080/api/auth/register';
const urlLogin = 'http://localhost:8080/api/auth/login';
const urlTransaction = 'http://localhost:8080/api/transactions';
const urlTransfer = 'http://localhost:8080/api/transfers';

const aliceCredentials = { username: "alice", password: "password" };
const bobCredentials = { username: "bob", password: "password" };

async function runStressTest() {
    console.log("🚀 Starting Custom Node.js Stress Test...");
    
    // 1. Register Users
    console.log("📝 Registering Alice and Bob...");
    try {
        await fetch(urlRegister, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(aliceCredentials) });
        await fetch(urlRegister, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(bobCredentials) });
    } catch(e) {
        console.error("❌ Failed to reach server. Is Spring Boot running?");
        return;
    }

    // 2. Login Alice to get her token
    console.log("🔐 Authenticating Alice...");
    const loginRes = await fetch(urlLogin, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(aliceCredentials) });
    const { token } = await loginRes.json();

    // 3. Fire 50 simultaneous transfers
    console.log("💣 Firing 50 simultaneous $100 transfer requests to try and double-spend...");
    const requests = [];
    
    for (let i = 0; i < 50; i++) {
        requests.push(
            fetch(urlTransfer, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                // Assuming Alice is ID 1 and Bob is ID 2 after fresh registration
                body: JSON.stringify({ fromUserId: 1, toUserId: 2, amount: 100.00, description: "Stress test transfer" })
            })
        );
    }

    const results = await Promise.all(requests.map(p => p.catch(e => e)));
    
    let successCount = 0;
    let failCount = 0;

    for (const res of results) {
        if (res && res.ok) successCount++;
        else failCount++;
    }

    console.log(`\n📊 RESULTS:`);
    console.log(`✅ Successful Transfers: ${successCount} (Should only be 10 if @Transactional is working perfectly)`);
    console.log(`❌ Failed Transfers: ${failCount} (Should be 40)`);
    console.log("\nIf successful transfers > 10, you have a race condition vulnerability!");
}

runStressTest();

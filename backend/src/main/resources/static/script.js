// =============================================
// JOB PORTAL — FRONTEND APPLICATION LOGIC
// =============================================

// ── API Base URL (auto-detect: same origin when deployed) ──
const API_BASE = window.location.origin + '/api';

// ── State ──
let currentUser = null;
let testSession = null;
let testQuestions = [];
let timerInterval = null;
let timeLeft = 600; // 10 minutes in seconds

// ══════════════════════════════════════════════
// UTILITY FUNCTIONS
// ══════════════════════════════════════════════

function showLoading(text = 'Loading...') {
    document.getElementById('loadingText').textContent = text;
    document.getElementById('loadingOverlay').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loadingOverlay').classList.add('hidden');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const icons = { success: '✅', error: '❌', info: 'ℹ️' };

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span class="toast-icon">${icons[type]}</span>
        <span>${message}</span>
        <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
    `;

    container.appendChild(toast);

    // Auto remove after 4 seconds
    setTimeout(() => {
        toast.style.animation = 'toastOut 0.4s ease-out forwards';
        setTimeout(() => toast.remove(), 400);
    }, 4000);
}

async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: { 'Content-Type': 'application/json' }
    };

    if (body) {
        options.body = JSON.stringify(body);
    }

    const response = await fetch(API_BASE + endpoint, options);
    const data = await response.json();
    return data;
}

// ══════════════════════════════════════════════
// AUTH — Login & Register
// ══════════════════════════════════════════════

function switchAuthTab(tab) {
    const loginTab = document.getElementById('loginTab');
    const registerTab = document.getElementById('registerTab');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    if (tab === 'login') {
        loginTab.classList.add('active');
        registerTab.classList.remove('active');
        loginForm.classList.remove('hidden');
        registerForm.classList.add('hidden');
    } else {
        registerTab.classList.add('active');
        loginTab.classList.remove('active');
        registerForm.classList.remove('hidden');
        loginForm.classList.add('hidden');
    }
}

async function handleLogin(event) {
    event.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    const btn = document.getElementById('loginBtn');

    btn.disabled = true;
    btn.innerHTML = '<div class="spinner"></div> Signing in...';

    try {
        const res = await apiCall('/users/login', 'POST', { email, password });

        if (res.success) {
            currentUser = res.data;
            localStorage.setItem('user', JSON.stringify(currentUser));
            showToast(`Welcome back, ${currentUser.name}!`, 'success');
            showApp();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Connection error. Is the server running?', 'error');
    }

    btn.disabled = false;
    btn.innerHTML = '🔐 Sign In';
}

async function handleRegister(event) {
    event.preventDefault();
    const name = document.getElementById('regName').value;
    const email = document.getElementById('regEmail').value;
    const password = document.getElementById('regPassword').value;
    const role = document.getElementById('regRole').value;
    const btn = document.getElementById('registerBtn');

    btn.disabled = true;
    btn.innerHTML = '<div class="spinner"></div> Creating account...';

    try {
        const res = await apiCall('/users/register', 'POST', { name, email, password, role });

        if (res.success) {
            currentUser = res.data;
            localStorage.setItem('user', JSON.stringify(currentUser));
            showToast('Account created successfully!', 'success');
            showApp();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Connection error. Is the server running?', 'error');
    }

    btn.disabled = false;
    btn.innerHTML = '🚀 Create Account';
}

function handleLogout() {
    currentUser = null;
    localStorage.removeItem('user');
    clearInterval(timerInterval);
    document.getElementById('appPage').classList.add('hidden');
    document.getElementById('authPage').classList.remove('hidden');
    showToast('Logged out successfully', 'info');
}

// ══════════════════════════════════════════════
// APP — Show main app after login
// ══════════════════════════════════════════════

function showApp() {
    document.getElementById('authPage').classList.add('hidden');
    document.getElementById('appPage').classList.remove('hidden');

    // Update nav user info
    document.getElementById('userName').textContent = currentUser.name;
    document.getElementById('userAvatar').textContent = currentUser.name.charAt(0).toUpperCase();
    document.getElementById('dashWelcome').textContent = currentUser.name.split(' ')[0];

    // Show admin features
    if (currentUser.role === 'ADMIN') {
        document.getElementById('adminJobForm').classList.remove('hidden');
    } else {
        document.getElementById('adminJobForm').classList.add('hidden');
    }

    showView('dashboard');
}

// ══════════════════════════════════════════════
// NAVIGATION
// ══════════════════════════════════════════════

function showView(view) {
    // Hide all views
    ['dashboardView', 'jobsView', 'testView', 'resultsView'].forEach(v => {
        document.getElementById(v).classList.add('hidden');
    });

    // Remove active state from nav
    ['navDashboard', 'navJobs', 'navTest', 'navResults'].forEach(n => {
        document.getElementById(n).style.background = '';
        document.getElementById(n).style.color = '';
    });

    // Show selected view
    document.getElementById(view + 'View').classList.remove('hidden');

    // Set active nav
    const navMap = { dashboard: 'navDashboard', jobs: 'navJobs', test: 'navTest', results: 'navResults' };
    const activeNav = document.getElementById(navMap[view]);
    if (activeNav) {
        activeNav.style.background = 'var(--bg-glass)';
        activeNav.style.color = 'var(--text-primary)';
    }

    // Load data for view
    if (view === 'dashboard') loadDashboard();
    if (view === 'jobs') loadJobs();
    if (view === 'test') resetTestView();
    if (view === 'results') loadAllResults();
}

// ══════════════════════════════════════════════
// DASHBOARD
// ══════════════════════════════════════════════

async function loadDashboard() {
    try {
        // Fetch jobs count
        const jobsRes = await apiCall('/jobs');
        if (jobsRes.success) {
            document.getElementById('statJobs').textContent = jobsRes.data.length;
        }

        // Fetch applications
        const appsRes = await apiCall(`/jobs/applications/${currentUser.id}`);
        if (appsRes.success) {
            document.getElementById('statApplied').textContent = appsRes.data.length;
        }

        // Fetch results
        const resultsRes = await apiCall(`/results/user/${currentUser.id}`);
        if (resultsRes.success) {
            const results = resultsRes.data;
            document.getElementById('statTests').textContent = results.length;

            // Calculate average
            if (results.length > 0) {
                const avg = results.reduce((sum, r) => sum + r.percentage, 0) / results.length;
                document.getElementById('statAvg').textContent = Math.round(avg) + '%';
            } else {
                document.getElementById('statAvg').textContent = '0%';
            }

            // Recent results table
            renderRecentResults(results.slice(0, 5));
        }
    } catch (err) {
        console.error('Dashboard error:', err);
    }
}

function renderRecentResults(results) {
    const container = document.getElementById('recentResults');

    if (results.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">📊</div>
                <h3>No tests taken yet</h3>
                <p>Take an assessment to see your results here</p>
            </div>`;
        return;
    }

    let html = `<table class="results-table">
        <thead>
            <tr>
                <th>#</th>
                <th>Score</th>
                <th>Total</th>
                <th>Percentage</th>
                <th>Date</th>
            </tr>
        </thead>
        <tbody>`;

    results.forEach((r, i) => {
        const date = new Date(r.completedAt).toLocaleDateString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric'
        });
        const pctColor = r.percentage >= 70 ? 'var(--success)' : r.percentage >= 40 ? 'var(--warning)' : 'var(--danger)';

        html += `<tr>
            <td>${i + 1}</td>
            <td><b>${r.score}</b></td>
            <td>${r.totalQuestions}</td>
            <td style="color:${pctColor}; font-weight:700;">${r.percentage}%</td>
            <td style="color:var(--text-muted);">${date}</td>
        </tr>`;
    });

    html += '</tbody></table>';
    container.innerHTML = html;
}

// ══════════════════════════════════════════════
// JOBS
// ══════════════════════════════════════════════

async function loadJobs() {
    const container = document.getElementById('jobsList');
    container.innerHTML = '<div class="text-center"><div class="spinner-lg" style="margin:2rem auto; border: 3px solid rgba(108,99,255,0.2); border-top-color: var(--primary);"></div></div>';

    try {
        const res = await apiCall('/jobs');
        if (res.success) {
            renderJobs(res.data);
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load jobs</h3></div>';
    }
}

function renderJobs(jobs) {
    const container = document.getElementById('jobsList');

    if (jobs.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">💼</div>
                <h3>No jobs available</h3>
                <p>Check back later for new opportunities</p>
            </div>`;
        return;
    }

    container.innerHTML = jobs.map((job, i) => `
        <div class="job-card" style="animation-delay: ${i * 0.1}s">
            <div class="job-company">
                <div class="company-icon">${job.company.charAt(0)}</div>
                ${job.company}
            </div>
            <h3>${job.title}</h3>
            <p class="job-desc">${job.description}</p>
            <div class="job-meta">
                <span>📍 ${job.location || 'Remote'}</span>
                ${job.salary ? `<span>💰 ${job.salary}</span>` : ''}
            </div>
            ${currentUser.role === 'STUDENT' ? `
                <button class="btn btn-primary btn-sm" onclick="applyJob(${job.id})" id="applyBtn${job.id}">
                    📩 Apply Now
                </button>
            ` : ''}
        </div>
    `).join('');
}

async function applyJob(jobId) {
    const btn = document.getElementById('applyBtn' + jobId);
    btn.disabled = true;
    btn.innerHTML = '<div class="spinner"></div>';

    try {
        const res = await apiCall('/jobs/apply', 'POST', {
            userId: currentUser.id,
            jobId: jobId
        });

        if (res.success) {
            showToast('Application submitted successfully! 🎉', 'success');
            btn.innerHTML = '✅ Applied';
            btn.classList.remove('btn-primary');
            btn.classList.add('btn-secondary');
        } else {
            showToast(res.message, 'error');
            btn.disabled = false;
            btn.innerHTML = '📩 Apply Now';
        }
    } catch (err) {
        showToast('Failed to apply', 'error');
        btn.disabled = false;
        btn.innerHTML = '📩 Apply Now';
    }
}

async function handleCreateJob(event) {
    event.preventDefault();

    const job = {
        title: document.getElementById('jobTitle').value,
        description: document.getElementById('jobDesc').value,
        company: document.getElementById('jobCompany').value,
        location: document.getElementById('jobLocation').value,
        salary: document.getElementById('jobSalary').value,
        createdBy: currentUser.id
    };

    try {
        const res = await apiCall('/jobs', 'POST', job);
        if (res.success) {
            showToast('Job posted successfully!', 'success');
            event.target.reset();
            loadJobs();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to create job', 'error');
    }
}

// ══════════════════════════════════════════════
// TEST SYSTEM
// ══════════════════════════════════════════════

function resetTestView() {
    document.getElementById('testStart').classList.remove('hidden');
    document.getElementById('testActive').classList.add('hidden');
    clearInterval(timerInterval);
    testSession = null;
    testQuestions = [];
    timeLeft = 600;
}

async function startTest() {
    const btn = document.getElementById('startTestBtn');
    btn.disabled = true;
    btn.innerHTML = '<div class="spinner"></div> Preparing test...';

    try {
        const res = await apiCall('/test/start', 'POST', { userId: currentUser.id });

        if (res.success) {
            testSession = res.data;
            testQuestions = testSession.questions;

            document.getElementById('testStart').classList.add('hidden');
            document.getElementById('testActive').classList.remove('hidden');
            document.getElementById('totalQ').textContent = testQuestions.length;

            renderQuestions();
            startTimer();

            showToast('Test started! Good luck! 🍀', 'info');
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to start test', 'error');
    }

    btn.disabled = false;
    btn.innerHTML = '🚀 Start Test';
}

function renderQuestions() {
    const container = document.getElementById('questionsContainer');

    container.innerHTML = testQuestions.map((q, i) => `
        <div class="question-card" style="animation-delay: ${i * 0.05}s">
            <div class="question-number">${i + 1}</div>
            <div class="question-text">${q.questionText}</div>
            <div class="options-grid">
                ${['A', 'B', 'C', 'D'].map(opt => `
                    <label class="option-label" id="opt_${q.id}_${opt}" onclick="selectOption(${q.id}, '${opt}', this)">
                        <input type="radio" name="q_${q.id}" value="${opt}">
                        <span class="option-indicator"></span>
                        <span class="option-text">${q['option' + opt]}</span>
                    </label>
                `).join('')}
            </div>
        </div>
    `).join('');

    updateProgress();
}

function selectOption(questionId, option, element) {
    // Clear previous selection for this question
    document.querySelectorAll(`[id^="opt_${questionId}_"]`).forEach(el => {
        el.classList.remove('selected');
    });

    // Mark selected
    element.classList.add('selected');

    // Update progress
    updateProgress();
}

function updateProgress() {
    const answered = document.querySelectorAll('.option-label.selected').length;
    // Count unique questions answered
    const uniqueAnswered = new Set();
    document.querySelectorAll('.option-label.selected').forEach(el => {
        const qId = el.id.split('_')[1];
        uniqueAnswered.add(qId);
    });

    const total = testQuestions.length;
    const pct = (uniqueAnswered.size / total) * 100;

    document.getElementById('progressFill').style.width = pct + '%';
    document.getElementById('currentQ').textContent = uniqueAnswered.size;
}

function startTimer() {
    timeLeft = 600;
    updateTimerDisplay();

    timerInterval = setInterval(() => {
        timeLeft--;
        updateTimerDisplay();

        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            showToast('⏰ Time is up! Auto-submitting...', 'error');
            submitTest();
        }
    }, 1000);
}

function updateTimerDisplay() {
    const minutes = Math.floor(timeLeft / 60);
    const seconds = timeLeft % 60;
    const display = `⏱ ${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;

    const timerEl = document.getElementById('timerDisplay');
    timerEl.textContent = display;

    // Visual warnings
    timerEl.className = 'timer';
    if (timeLeft <= 60) timerEl.classList.add('danger');
    else if (timeLeft <= 180) timerEl.classList.add('warning');
}

async function submitTest() {
    clearInterval(timerInterval);

    const btn = document.getElementById('submitTestBtn');
    btn.disabled = true;
    btn.innerHTML = '<div class="spinner"></div> Submitting...';

    // Collect answers
    const answers = {};
    testQuestions.forEach(q => {
        const selected = document.querySelector(`input[name="q_${q.id}"]:checked`);
        if (selected) {
            answers[String(q.id)] = selected.value;
        }
    });

    try {
        const res = await apiCall('/test/submit', 'POST', {
            sessionId: testSession.sessionId,
            userId: currentUser.id,
            answers: answers
        });

        if (res.success) {
            showToast('Test submitted successfully! 🎉', 'success');
            displayResult(res.data);
        } else {
            showToast(res.message, 'error');
            btn.disabled = false;
            btn.innerHTML = '✅ Submit Test';
        }
    } catch (err) {
        showToast('Failed to submit test', 'error');
        btn.disabled = false;
        btn.innerHTML = '✅ Submit Test';
    }
}

function displayResult(result) {
    // Switch to results view
    showView('results');

    // Show result display
    document.getElementById('resultDisplay').classList.remove('hidden');
    document.getElementById('allResultsSection').classList.add('hidden');

    // Populate values
    document.getElementById('resultScore').textContent = result.score;
    document.getElementById('resultTotal').textContent = result.totalQuestions;
    document.getElementById('resultCorrect').textContent = result.score;
    document.getElementById('resultWrong').textContent = result.totalQuestions - result.score;
    document.getElementById('resultPercentage').textContent = result.percentage + '%';

    // Message based on score
    const msgEl = document.getElementById('resultMessage');
    const pct = result.percentage;

    if (pct >= 80) {
        msgEl.className = 'result-message excellent';
        msgEl.textContent = '🌟 Excellent! Outstanding performance!';
    } else if (pct >= 60) {
        msgEl.className = 'result-message good';
        msgEl.textContent = '👍 Good job! Keep improving!';
    } else if (pct >= 40) {
        msgEl.className = 'result-message average';
        msgEl.textContent = '📚 Average. More practice needed!';
    } else {
        msgEl.className = 'result-message poor';
        msgEl.textContent = '💪 Keep trying! Study and retake!';
    }

    // Set the score circle border color
    const circle = document.querySelector('.score-circle');
    if (pct >= 80) circle.style.borderColor = 'var(--success)';
    else if (pct >= 60) circle.style.borderColor = 'var(--primary)';
    else if (pct >= 40) circle.style.borderColor = 'var(--warning)';
    else circle.style.borderColor = 'var(--danger)';
}

// ══════════════════════════════════════════════
// RESULTS
// ══════════════════════════════════════════════

async function loadAllResults() {
    document.getElementById('resultDisplay').classList.add('hidden');
    document.getElementById('allResultsSection').classList.remove('hidden');

    const container = document.getElementById('allResultsList');

    try {
        const res = await apiCall(`/results/user/${currentUser.id}`);
        if (res.success) {
            const results = res.data;

            if (results.length === 0) {
                container.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-icon">🏆</div>
                        <h3>No results yet</h3>
                        <p>Take an assessment to see your scores here</p>
                        <button class="btn btn-primary mt-2" onclick="showView('test')">📝 Take a Test</button>
                    </div>`;
                return;
            }

            let html = `<table class="results-table">
                <thead>
                    <tr><th>#</th><th>Score</th><th>Total</th><th>Percentage</th><th>Grade</th><th>Date</th></tr>
                </thead>
                <tbody>`;

            results.forEach((r, i) => {
                const date = new Date(r.completedAt).toLocaleDateString('en-IN', {
                    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
                });
                const pctColor = r.percentage >= 70 ? 'var(--success)' : r.percentage >= 40 ? 'var(--warning)' : 'var(--danger)';
                let grade = '💪 Needs Work';
                if (r.percentage >= 80) grade = '🌟 Excellent';
                else if (r.percentage >= 60) grade = '👍 Good';
                else if (r.percentage >= 40) grade = '📚 Average';

                html += `<tr>
                    <td>${i + 1}</td>
                    <td><b>${r.score}</b></td>
                    <td>${r.totalQuestions}</td>
                    <td style="color:${pctColor}; font-weight:700;">${r.percentage}%</td>
                    <td>${grade}</td>
                    <td style="color:var(--text-muted); font-size:0.85rem;">${date}</td>
                </tr>`;
            });

            html += '</tbody></table>';
            container.innerHTML = html;
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load results</h3></div>';
    }
}

// ══════════════════════════════════════════════
// INIT — Check saved session
// ══════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', () => {
    const saved = localStorage.getItem('user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            showApp();
        } catch (e) {
            localStorage.removeItem('user');
        }
    }
});

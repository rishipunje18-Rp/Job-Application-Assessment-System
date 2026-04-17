const API_URL = 'http://localhost:8081/api';
let currentUser = JSON.parse(localStorage.getItem('user')) || null;
let currentTestSessionId = null;
let currentTestAnswers = {};

// ================= UI UTILITIES =================
function showLoading() { document.getElementById('loadingOverlay').classList.remove('hidden'); }
function hideLoading() { document.getElementById('loadingOverlay').classList.add('hidden'); }
function closeModals() { document.querySelectorAll('.loading-overlay').forEach(el => {
  if(el.id !== 'loadingOverlay') el.classList.add('hidden');
}); }

function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  let icon = 'ℹ️';
  if(type === 'success') icon = '✅';
  if(type === 'error') icon = '❌';
  
  toast.innerHTML = `
    <span class="toast-icon">${icon}</span>
    <span style="flex-grow: 1">${message}</span>
    <button class="toast-close" onclick="this.parentElement.remove()">✕</button>
  `;
  container.appendChild(toast);
  setTimeout(() => {
    toast.style.animation = 'toastOut 0.4s ease-in forwards';
    setTimeout(() => toast.remove(), 400);
  }, 4000);
}

async function apiCall(endpoint, method = 'GET', body = null) {
  try {
    showLoading();
    const options = { method, headers: { 'Content-Type': 'application/json' } };
    if (body) options.body = JSON.stringify(body);
    
    const res = await fetch(`${API_URL}${endpoint}`, options);
    const data = await res.json();
    hideLoading();
    
    if (!res.ok || !data.success) {
      throw new Error(data.message || 'API Error');
    }
    return data.data;
  } catch (error) {
    hideLoading();
    showToast(error.message, 'error');
    throw error;
  }
}

// ================= AUTHENTICATION =================
function checkAuthStatus() {
  if (currentUser) {
    document.getElementById('authPage').classList.add('hidden');
    document.getElementById('appPage').classList.remove('hidden');
    
    document.getElementById('userNameDisplay').innerText = currentUser.name;
    document.getElementById('userAvatar').innerText = currentUser.name.charAt(0).toUpperCase();
    
    const roleBadge = document.getElementById('userRoleBadge');
    roleBadge.innerText = currentUser.role;
    roleBadge.className = `badge-role ${currentUser.role.toLowerCase()}`;

    if (currentUser.role === 'ADMIN') {
      document.getElementById('adminNavLinks').classList.remove('hidden');
      document.getElementById('studentNavLinks').classList.add('hidden');
      router('adminDashboard');
    } else {
      document.getElementById('adminNavLinks').classList.add('hidden');
      document.getElementById('studentNavLinks').classList.remove('hidden');
      router('studentDashboard');
    }
  } else {
    document.getElementById('appPage').classList.add('hidden');
    document.getElementById('authPage').classList.remove('hidden');
  }
}

function switchAuthMode(mode) {
  const isLogin = mode === 'login';
  document.getElementById('tabLogin').classList.toggle('active', isLogin);
  document.getElementById('tabRegister').classList.toggle('active', !isLogin);
  document.getElementById('registerFields').classList.toggle('hidden', isLogin);
  document.getElementById('nameInput').required = !isLogin;
  document.getElementById('authSubmitBtn').innerText = isLogin ? 'Sign In' : 'Create Account';
}

async function handleAuth(e) {
  e.preventDefault();
  const isLogin = document.getElementById('tabLogin').classList.contains('active');
  const email = document.getElementById('emailInput').value;
  const password = document.getElementById('passwordInput').value;

  try {
    if (isLogin) {
      const user = await apiCall('/users/login', 'POST', { email, password });
      currentUser = user;
    } else {
      const name = document.getElementById('nameInput').value;
      const role = document.getElementById('roleInput').value;
      const user = await apiCall('/users/register', 'POST', { name, email, password, role });
      currentUser = user;
    }
    localStorage.setItem('user', JSON.stringify(currentUser));
    showToast(`Welcome back, ${currentUser.name}!`, 'success');
    checkAuthStatus();
  } catch (error) {}
}

function logout() {
  localStorage.removeItem('user');
  currentUser = null;
  checkAuthStatus();
}

// ================= ROUTER =================
function router(viewId) {
  document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
  const view = document.getElementById(viewId + 'View');
  if (view) view.classList.remove('hidden');

  // Load specific data based on view
  if (viewId === 'adminDashboard') loadAdminDashboard();
  if (viewId === 'adminJobs') loadAdminJobs();
  if (viewId === 'adminApplicants') loadAdminApplicants();
  if (viewId === 'adminTests') loadAdminTests();
  if (viewId === 'adminResults') loadAdminResults();

  if (viewId === 'studentDashboard') loadStudentDashboard();
  if (viewId === 'studentJobs') loadStudentJobs();
  if (viewId === 'studentTests') loadStudentTests();
  if (viewId === 'studentResults') loadStudentResults();
}

// ================= ADMIN FUNCTIONS =================
async function loadAdminDashboard() {
  try {
    const stats = await apiCall(`/users/stats/${currentUser.id}`);
    document.getElementById('statTotalJobs').innerText = stats.totalJobs;
    document.getElementById('statTotalApplicants').innerText = stats.totalApplicants;
    document.getElementById('statTestsCreated').innerText = stats.testsCreated;
    document.getElementById('statTotalResults').innerText = stats.totalResults;
  } catch (e) {}
}

async function loadAdminJobs() {
  try {
    const jobs = await apiCall('/jobs');
    const grid = document.getElementById('adminJobsGrid');
    grid.innerHTML = jobs.map(j => `
      <div class="job-card">
        <div class="job-company">
          <div class="company-icon">${j.company.charAt(0)}</div>
          ${j.company}
        </div>
        <h3>${j.title}</h3>
        <div class="job-meta">
          <span>📍 ${j.location}</span>
          <span>💰 ${j.salary}</span>
        </div>
        <div class="flex-between mt-1">
          <p class="text-muted" style="font-size: 0.8rem">Posted: ${new Date(j.createdAt).toLocaleDateString()}</p>
          <div>
            <button class="btn btn-sm btn-danger" onclick="deleteJob(${j.id})">Delete</button>
          </div>
        </div>
      </div>
    `).join('') || '<p>No jobs found. Create one!</p>';
  } catch(e) {}
}

function showCreateJobModal() {
  document.getElementById('jobTitleInp').value = '';
  document.getElementById('jobCompanyInp').value = '';
  document.getElementById('jobLocationInp').value = '';
  document.getElementById('jobSalaryInp').value = '';
  document.getElementById('jobDescInp').value = '';
  document.getElementById('createJobModal').classList.remove('hidden');
}

async function handleCreateJob(e) {
  e.preventDefault();
  const job = {
    title: document.getElementById('jobTitleInp').value,
    company: document.getElementById('jobCompanyInp').value,
    location: document.getElementById('jobLocationInp').value,
    salary: document.getElementById('jobSalaryInp').value,
    description: document.getElementById('jobDescInp').value,
  };
  try {
    await apiCall(`/jobs?adminId=${currentUser.id}`, 'POST', job);
    showToast('Job created!', 'success');
    closeModals();
    loadAdminJobs();
  } catch(e) {}
}

async function deleteJob(id) {
  if(!confirm('Delete this job?')) return;
  try {
    await apiCall(`/jobs/${id}?adminId=${currentUser.id}`, 'DELETE');
    showToast('Job deleted', 'success');
    loadAdminJobs();
  } catch(e) {}
}

async function loadAdminApplicants() {
  try {
    const apps = await apiCall('/jobs/applications/all');
    const tbody = document.querySelector('#adminApplicantsTable tbody');
    tbody.innerHTML = apps.map(a => `
      <tr>
        <td>
          <div style="font-weight:600">${a.userName}</div>
          <div class="text-muted" style="font-size:0.8rem">${a.userEmail}</div>
        </td>
        <td>${a.jobTitle}</td>
        <td>${new Date(a.appliedAt).toLocaleDateString()}</td>
        <td><span class="status-pill status-${a.status}">${a.status}</span></td>
        <td>
          <select onchange="updateAppStatus(${a.id}, this.value)" style="padding:0.2rem; border-radius:4px" ${a.status !== 'PENDING' ? 'disabled' : ''}>
            <option value="PENDING" ${a.status === 'PENDING'?'selected':''}>Pending</option>
            <option value="SELECTED" ${a.status === 'SELECTED'?'selected':''}>Select</option>
            <option value="REJECTED" ${a.status === 'REJECTED'?'selected':''}>Reject</option>
          </select>
        </td>
      </tr>
    `).join('') || '<tr><td colspan="5" class="text-center">No applications found.</td></tr>';
  } catch(e) {}
}

async function updateAppStatus(id, newStatus) {
  try {
    await apiCall(`/jobs/applications/${id}/status?adminId=${currentUser.id}`, 'PUT', {status: newStatus});
    showToast(`Status updated to ${newStatus}`, 'success');
    loadAdminApplicants();
  } catch(e) {}
}

async function loadAdminTests() {
  try {
    const tests = await apiCall(`/test/created/${currentUser.id}`);
    const grid = document.getElementById('adminTestsGrid');
    grid.innerHTML = tests.map(t => `
      <div class="test-card">
        <div class="test-info">
          <h3>${t.title}</h3>
          <p>Assigned to: ${t.assignedUserId}</p>
        </div>
        <div><span class="status-pill status-${t.status}">${t.status}</span></div>
      </div>
    `).join('') || '<p>No tests created yet.</p>';
  } catch(e) {}
}

async function showCreateTestModal() {
  try {
    // Load jobs & students for dropdowns
    const [jobs, students] = await Promise.all([
      apiCall('/jobs'),
      apiCall('/users/students')
    ]);
    const jSelect = document.getElementById('testJobInp');
    jSelect.innerHTML = '<option value="">-- Optional Job Link --</option>' + jobs.map(j => `<option value="${j.id}">${j.title} (${j.company})</option>`).join('');
    
    const sSelect = document.getElementById('testStudentInp');
    sSelect.innerHTML = '<option value="">-- Select Student --</option>' + students.map(s => `<option value="${s.id}">${s.name} (${s.email})</option>`).join('');

    document.getElementById('testTitleInp').value = '';
    document.getElementById('testQuestionsList').innerHTML = getQBlockHTML(); // Reset to 1 question
    document.getElementById('createTestModal').classList.remove('hidden');
  } catch(e) {}
}

function getQBlockHTML() {
  return `
    <div class="glass-card mb-2 q-block" style="padding: 1rem;">
      <div class="form-group"><label>Question Text</label><input type="text" class="q-text" required></div>
      <div style="display:grid; grid-template-columns: 1fr 1fr; gap:0.5rem">
        <div><label>Opt A</label><input type="text" class="q-optA" required></div>
        <div><label>Opt B</label><input type="text" class="q-optB" required></div>
        <div><label>Opt C</label><input type="text" class="q-optC" required></div>
        <div><label>Opt D</label><input type="text" class="q-optD" required></div>
      </div>
      <div class="form-group mt-1">
        <label>Correct Answer</label>
        <select class="q-ans" required>
          <option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option>
        </select>
      </div>
    </div>
  `;
}
function addQuestionBlock() {
  document.getElementById('testQuestionsList').insertAdjacentHTML('beforeend', getQBlockHTML());
}

async function handleCreateTest(e) {
  e.preventDefault();
  const title = document.getElementById('testTitleInp').value;
  const jobId = document.getElementById('testJobInp').value;
  const assignedUserId = document.getElementById('testStudentInp').value;
  
  const blocks = document.querySelectorAll('.q-block');
  const questions = Array.from(blocks).map(b => ({
    questionText: b.querySelector('.q-text').value,
    optionA: b.querySelector('.q-optA').value,
    optionB: b.querySelector('.q-optB').value,
    optionC: b.querySelector('.q-optC').value,
    optionD: b.querySelector('.q-optD').value,
    correctAnswer: b.querySelector('.q-ans').value
  }));

  try {
    await apiCall(`/test/create?adminId=${currentUser.id}`, 'POST', {
      title, jobId: jobId || null, assignedUserId, questions
    });
    showToast('Test created & assigned!', 'success');
    closeModals();
    loadAdminTests();
  } catch(e){}
}

async function loadAdminResults() {
  try {
    const results = await apiCall('/results/all');
    const tbody = document.querySelector('#adminResultsTable tbody');
    tbody.innerHTML = results.map(r => `
      <tr>
        <td>
          <div style="font-weight:600">${r.userName}</div>
          <div class="text-muted" style="font-size:0.8rem">${r.userEmail}</div>
        </td>
        <td>${r.testTitle}</td>
        <td><strong>${r.score}/${r.totalQuestions} (${r.percentage}%)</strong></td>
        <td><span class="status-pill status-${r.status}">${r.status}</span></td>
        <td>
          <select onchange="updateResStatus(${r.id}, this.value)" style="padding:0.2rem; border-radius:4px" ${r.status !== 'PENDING' ? 'disabled' : ''}>
            <option value="PENDING" ${r.status === 'PENDING'?'selected':''}>Pending</option>
            <option value="SELECTED" ${r.status === 'SELECTED'?'selected':''}>Select</option>
            <option value="REJECTED" ${r.status === 'REJECTED'?'selected':''}>Reject</option>
          </select>
        </td>
      </tr>
    `).join('') || '<tr><td colspan="5" class="text-center">No results found.</td></tr>';
  } catch(e) {}
}

async function updateResStatus(id, newStatus) {
  try {
    await apiCall(`/results/${id}/status?adminId=${currentUser.id}`, 'PUT', {status: newStatus});
    showToast(`Result status updated to ${newStatus}`, 'success');
    loadAdminResults();
  } catch(e) {}
}


// ================= STUDENT FUNCTIONS =================
async function loadStudentDashboard() {
  document.getElementById('studentWelcomeName').innerText = currentUser.name.split(' ')[0];
  try {
    const stats = await apiCall(`/users/stats/${currentUser.id}`);
    document.getElementById('statAvailableJobs').innerText = stats.availableJobs;
    document.getElementById('statMyApplications').innerText = stats.applications;
    document.getElementById('statMyTests').innerText = stats.testsAssigned;
    document.getElementById('statMyResults').innerText = stats.testsTaken;
  } catch(e){}
}

async function loadStudentJobs() {
  try {
    const jobs = await apiCall('/jobs');
    const grid = document.getElementById('studentJobsGrid');
    grid.innerHTML = jobs.map(j => `
      <div class="job-card">
        <div class="job-company">
          <div class="company-icon">${j.company.charAt(0)}</div>
          ${j.company}
        </div>
        <h3>${j.title}</h3>
        <p class="job-desc">${j.description}</p>
        <div class="job-meta">
          <span>📍 ${j.location}</span>
          <span>💰 ${j.salary}</span>
        </div>
        <button class="btn btn-primary btn-full" onclick="applyJob(${j.id})">Apply Now</button>
      </div>
    `).join('') || '<p>No jobs available.</p>';
  } catch(e) {}
}

async function applyJob(jobId) {
  try {
    await apiCall('/jobs/apply', 'POST', { userId: currentUser.id, jobId });
    showToast('Applied successfully!', 'success');
    loadStudentDashboard();
  } catch(e) {}
}

async function loadStudentTests() {
  try {
    const tests = await apiCall(`/test/assigned/${currentUser.id}`);
    const grid = document.getElementById('studentTestsGrid');
    const pendingTests = tests.filter(t => t.status !== 'COMPLETED');
    grid.innerHTML = pendingTests.map(t => `
      <div class="job-card">
        <h3>${t.title}</h3>
        <p class="text-muted mb-1">Status: <span class="status-pill status-${t.status}">${t.status}</span></p>
        <button class="btn btn-success btn-full" onclick="startTest(${t.id})">Start Assessment</button>
      </div>
    `).join('') || '<p>No pending tests assigned to you.</p>';
  } catch(e) {}
}

async function startTest(sessionId) {
  if(!confirm("Are you ready to start the test? You cannot pause it once started.")) return;
  try {
    const sessionData = await apiCall('/test/start', 'POST', { userId: currentUser.id, sessionId });
    currentTestSessionId = sessionData.sessionId;
    currentTestAnswers = {};
    renderTestInterface(sessionData);
  } catch(e) {}
}

function renderTestInterface(data) {
  document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
  document.getElementById('testTakingView').classList.remove('hidden');
  document.getElementById('takingTestTitle').innerText = data.title;
  
  const container = document.getElementById('testQuestionContainer');
  container.innerHTML = data.questions.map((q, idx) => `
    <div class="question-card">
      <div class="question-number">${idx + 1}</div>
      <div class="question-text">${q.questionText}</div>
      <div class="options-grid">
        <label class="option-label" onclick="selectAnswer(${q.id}, 'A', this)">
          <input type="radio" name="q_${q.id}" value="A">
          <div class="option-indicator"></div><span class="option-text">${q.optionA}</span>
        </label>
        <label class="option-label" onclick="selectAnswer(${q.id}, 'B', this)">
          <input type="radio" name="q_${q.id}" value="B">
          <div class="option-indicator"></div><span class="option-text">${q.optionB}</span>
        </label>
        <label class="option-label" onclick="selectAnswer(${q.id}, 'C', this)">
          <input type="radio" name="q_${q.id}" value="C">
          <div class="option-indicator"></div><span class="option-text">${q.optionC}</span>
        </label>
        <label class="option-label" onclick="selectAnswer(${q.id}, 'D', this)">
          <input type="radio" name="q_${q.id}" value="D">
          <div class="option-indicator"></div><span class="option-text">${q.optionD}</span>
        </label>
      </div>
    </div>
  `).join('');
  document.getElementById('questionProgress').innerText = `Total Questions: ${data.questions.length}`;
}

function selectAnswer(qId, val, labelEl) {
  currentTestAnswers[qId] = val;
  const parent = labelEl.closest('.options-grid');
  parent.querySelectorAll('.option-label').forEach(l => l.classList.remove('selected'));
  labelEl.classList.add('selected');
}

async function submitTest() {
  if(!confirm("Submit final answers?")) return;
  try {
    const result = await apiCall('/test/submit', 'POST', {
      userId: currentUser.id, sessionId: currentTestSessionId, answers: currentTestAnswers
    });
    showTestResult(result);
  } catch(e) {}
}

function showTestResult(result) {
  document.querySelectorAll('.view').forEach(v => v.classList.add('hidden'));
  const v = document.getElementById('resultDetailView');
  v.classList.remove('hidden');

  let cssClass = 'poor'; let msg = 'Needs Improvement';
  if (result.percentage >= 90) { cssClass = 'excellent'; msg = 'Excellent Performance!'; }
  else if (result.percentage >= 70) { cssClass = 'good'; msg = 'Good Job!'; }
  else if (result.percentage >= 50) { cssClass = 'average'; msg = 'Average Score.'; }

  document.getElementById('resultDetailContent').innerHTML = `
    <h2>Assessment Complete</h2>
    <div class="score-circle">
      <div class="score-value">${result.percentage}%</div>
      <div class="score-label">Final Score</div>
    </div>
    <div class="result-message ${cssClass}">${msg}</div>
    <div class="result-details">
      <div class="result-stat"><div class="stat-value text-primary">${result.score}</div><div class="stat-label">Correct Answers</div></div>
      <div class="result-stat"><div class="stat-value text-primary">${result.totalQuestions}</div><div class="stat-label">Total Questions</div></div>
      <div class="result-stat"><div class="stat-value ${result.status==='SELECTED'?'text-success':'text-warning'}">${result.status}</div><div class="stat-label">Selection Status</div></div>
    </div>
    <button class="btn btn-primary" onclick="router('studentDashboard')">Back to Dashboard</button>
  `;
}

async function loadStudentResults() {
  try {
    const results = await apiCall(`/results/user/${currentUser.id}`);
    const grid = document.getElementById('studentResultsGrid');
    grid.innerHTML = results.map(r => `
      <div class="job-card">
        <h3>${r.testTitle || 'Assessment'}</h3>
        <div class="flex-between mb-1 mt-1">
          <span style="font-size:1.5rem; font-weight:800; color:var(--primary)">${r.percentage}%</span>
          <span class="status-pill status-${r.status}">${r.status}</span>
        </div>
        <p class="text-muted" style="font-size:0.85rem">Score: ${r.score}/${r.totalQuestions} &bull; Taken: ${new Date(r.completedAt).toLocaleDateString()}</p>
      </div>
    `).join('') || '<p>No test results yet.</p>';
  } catch(e) {}
}

// ================= INIT =================
window.onload = () => {
  checkAuthStatus();
};

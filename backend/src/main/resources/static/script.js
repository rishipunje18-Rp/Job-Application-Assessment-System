// =============================================
// JOB PORTAL — COMPLETE FRONTEND APPLICATION
// Role-based: ADMIN and STUDENT views
// =============================================

// ── API Base URL ──
// Empty string = same origin (frontend served by Spring Boot)
const API_BASE = window.location.origin + '/api';

// ── Global State ──
let currentUser = null;    // Logged-in user object { id, name, email, role }
let testSession = null;    // Current active test session
let testQuestions = [];    // Questions for the active test
let timerInterval = null;  // Timer interval reference
let timeLeft = 600;        // 10 minutes in seconds
let questionCount = 0;     // Counter for dynamic question fields

// ══════════════════════════════════════════════
// UTILITY FUNCTIONS
// ══════════════════════════════════════════════

// Show a toast notification (success / error / info)
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    const icons = { success: '✅', error: '❌', info: 'ℹ️' };
    const toast = document.createElement('div');
    toast.className = 'toast ' + type;
    toast.innerHTML = '<span>' + (icons[type] || '') + ' ' + message + '</span>' +
        '<button class="toast-close" onclick="this.parentElement.remove()">✕</button>';
    container.appendChild(toast);
    // Auto-remove after 4 seconds
    setTimeout(function() {
        if (toast.parentElement) toast.remove();
    }, 4000);
}

// Generic API call helper — sends JSON and returns parsed response
async function apiCall(endpoint, method, body) {
    var options = {
        method: method || 'GET',
        headers: { 'Content-Type': 'application/json' }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }
    var response = await fetch(API_BASE + endpoint, options);
    return await response.json();
}

// ══════════════════════════════════════════════
// AUTH — Login & Register
// ══════════════════════════════════════════════

// Switch between Login and Register tabs
function switchAuthTab(tab) {
    document.getElementById('loginTab').classList.toggle('active', tab === 'login');
    document.getElementById('registerTab').classList.toggle('active', tab === 'register');
    document.getElementById('loginForm').classList.toggle('hidden', tab !== 'login');
    document.getElementById('registerForm').classList.toggle('hidden', tab !== 'register');
}

// Handle login form submission
async function handleLogin(event) {
    event.preventDefault();
    var email = document.getElementById('loginEmail').value;
    var password = document.getElementById('loginPassword').value;
    var btn = document.getElementById('loginBtn');

    btn.disabled = true;
    btn.textContent = 'Signing in...';

    try {
        var res = await apiCall('/users/login', 'POST', { email: email, password: password });
        if (res.success) {
            currentUser = res.data;
            localStorage.setItem('user', JSON.stringify(currentUser));
            showToast('Welcome back, ' + currentUser.name + '!', 'success');
            showApp();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Connection error. Is the server running?', 'error');
    }

    btn.disabled = false;
    btn.textContent = '🔐 Sign In';
}

// Handle registration form submission
async function handleRegister(event) {
    event.preventDefault();
    var name = document.getElementById('regName').value;
    var email = document.getElementById('regEmail').value;
    var password = document.getElementById('regPassword').value;
    var role = document.getElementById('regRole').value;
    var btn = document.getElementById('registerBtn');

    btn.disabled = true;
    btn.textContent = 'Creating account...';

    try {
        var res = await apiCall('/users/register', 'POST', {
            name: name, email: email, password: password, role: role
        });
        if (res.success) {
            showToast('Registration successful! Please login.', 'success');
            switchAuthTab('login');
            // Pre-fill login email
            document.getElementById('loginEmail').value = email;
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Connection error. Is the server running?', 'error');
    }

    btn.disabled = false;
    btn.textContent = '🚀 Create Account';
}

// Logout — clear session and show auth page
function handleLogout() {
    currentUser = null;
    localStorage.removeItem('user');
    clearInterval(timerInterval);
    document.getElementById('appPage').classList.add('hidden');
    document.getElementById('authPage').classList.remove('hidden');
    showToast('Logged out successfully', 'info');
}

// ══════════════════════════════════════════════
// APP INITIALIZATION — Show app after login
// ══════════════════════════════════════════════

function showApp() {
    document.getElementById('authPage').classList.add('hidden');
    document.getElementById('appPage').classList.remove('hidden');

    // Update nav bar with user info
    document.getElementById('userName').textContent = currentUser.name;
    document.getElementById('userAvatar').textContent = currentUser.name.charAt(0).toUpperCase();
    document.getElementById('userRole').textContent = currentUser.role;
    document.getElementById('userRole').className = 'role-badge ' + currentUser.role.toLowerCase();

    var isAdmin = currentUser.role === 'ADMIN';

    // Show/hide role-specific elements
    // Admin: show "Applications" nav, "Create Job" form, admin test section
    document.getElementById('navApplications').classList.toggle('hidden', !isAdmin);
    document.getElementById('adminJobForm').classList.toggle('hidden', !isAdmin);

    // Load dashboard
    showView('dashboard');
}

// ══════════════════════════════════════════════
// NAVIGATION — Switch between views
// ══════════════════════════════════════════════

function showView(view) {
    // Hide all views
    var views = ['dashboardView', 'jobsView', 'testsView', 'resultsView', 'applicationsView'];
    views.forEach(function(v) {
        document.getElementById(v).classList.add('hidden');
    });

    // Deactivate all nav links
    var navIds = ['navDashboard', 'navJobs', 'navApplications', 'navTests', 'navResults'];
    navIds.forEach(function(n) {
        document.getElementById(n).classList.remove('active');
    });

    // Show selected view
    document.getElementById(view + 'View').classList.remove('hidden');

    // Activate the matching nav link
    var navMap = {
        dashboard: 'navDashboard',
        jobs: 'navJobs',
        applications: 'navApplications',
        tests: 'navTests',
        results: 'navResults'
    };
    if (navMap[view]) {
        document.getElementById(navMap[view]).classList.add('active');
    }

    // Load data for the selected view
    switch (view) {
        case 'dashboard': loadDashboard(); break;
        case 'jobs': loadJobs(); break;
        case 'applications': loadApplications(); break;
        case 'tests': loadTests(); break;
        case 'results': loadResults(); break;
    }
}

// ══════════════════════════════════════════════
// DASHBOARD — Stats cards (role-specific data)
// ══════════════════════════════════════════════

async function loadDashboard() {
    document.getElementById('dashWelcome').textContent = currentUser.name;

    var isAdmin = currentUser.role === 'ADMIN';

    if (isAdmin) {
        // Admin sees: Total Jobs, Total Applications, Tests Created, Total Results
        document.getElementById('dashSubtitle').textContent = 'Admin Dashboard — System Overview';
        document.getElementById('statJobsLabel').textContent = 'Total Jobs';
        document.getElementById('statAppsLabel').textContent = 'Total Applications';
        document.getElementById('statTestsLabel').textContent = 'Tests Created';
        document.getElementById('statResultsLabel').textContent = 'Total Results';

        try {
            var jobsRes = await apiCall('/jobs');
            document.getElementById('statJobs').textContent = jobsRes.success ? jobsRes.data.length : 0;

            var appsRes = await apiCall('/jobs/all-applications');
            document.getElementById('statApps').textContent = appsRes.success ? appsRes.data.length : 0;

            var testsRes = await apiCall('/test/all');
            document.getElementById('statTests').textContent = testsRes.success ? testsRes.data.length : 0;

            var resultsRes = await apiCall('/results/all');
            document.getElementById('statResults').textContent = resultsRes.success ? resultsRes.data.length : 0;
        } catch (err) {
            console.error('Dashboard error:', err);
        }
    } else {
        // Student sees: Available Jobs, My Applications, Tests Assigned, Tests Completed
        document.getElementById('dashSubtitle').textContent = 'Student Dashboard — Your Activity';
        document.getElementById('statJobsLabel').textContent = 'Available Jobs';
        document.getElementById('statAppsLabel').textContent = 'My Applications';
        document.getElementById('statTestsLabel').textContent = 'Tests Assigned';
        document.getElementById('statResultsLabel').textContent = 'Tests Completed';

        try {
            var jobsRes2 = await apiCall('/jobs');
            document.getElementById('statJobs').textContent = jobsRes2.success ? jobsRes2.data.length : 0;

            var appsRes2 = await apiCall('/jobs/applications/' + currentUser.id);
            document.getElementById('statApps').textContent = appsRes2.success ? appsRes2.data.length : 0;

            var testsRes2 = await apiCall('/test/assigned/' + currentUser.id);
            document.getElementById('statTests').textContent = testsRes2.success ? testsRes2.data.length : 0;

            var resultsRes2 = await apiCall('/results/user/' + currentUser.id);
            document.getElementById('statResults').textContent = resultsRes2.success ? resultsRes2.data.length : 0;
        } catch (err) {
            console.error('Dashboard error:', err);
        }
    }
}

// ══════════════════════════════════════════════
// JOBS — View and manage job postings
// ══════════════════════════════════════════════

async function loadJobs() {
    var container = document.getElementById('jobsList');
    container.innerHTML = '<div class="text-center"><p>Loading jobs...</p></div>';

    try {
        var res = await apiCall('/jobs');
        if (res.success) {
            renderJobs(res.data);
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load jobs</h3></div>';
    }
}

// Render job cards (different buttons for admin vs student)
function renderJobs(jobs) {
    var container = document.getElementById('jobsList');
    var isAdmin = currentUser.role === 'ADMIN';

    if (jobs.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">💼</div>' +
            '<h3>No jobs available</h3><p>Check back later for new opportunities</p></div>';
        return;
    }

    var html = '';
    jobs.forEach(function(job) {
        html += '<div class="job-card">';
        html += '<div class="job-company">';
        html += '<div class="company-icon">' + job.company.charAt(0) + '</div>';
        html += job.company + '</div>';
        html += '<h3>' + job.title + '</h3>';
        html += '<p class="job-desc">' + job.description + '</p>';
        html += '<div class="job-meta">';
        html += '<span>📍 ' + (job.location || 'Remote') + '</span>';
        if (job.salary) html += '<span>💰 ' + job.salary + '</span>';
        html += '</div>';

        // Student sees "Apply" button, Admin sees "Delete" button
        if (!isAdmin) {
            html += '<button class="btn btn-primary btn-sm" onclick="applyJob(' + job.id + ')" id="applyBtn' + job.id + '">📩 Apply Now</button>';
        } else {
            html += '<button class="btn btn-danger btn-sm" onclick="deleteJob(' + job.id + ')">🗑 Delete</button>';
        }
        html += '</div>';
    });

    container.innerHTML = html;
}

// Student applies for a job
async function applyJob(jobId) {
    var btn = document.getElementById('applyBtn' + jobId);
    btn.disabled = true;
    btn.textContent = 'Applying...';

    try {
        var res = await apiCall('/jobs/apply', 'POST', {
            userId: currentUser.id,
            jobId: jobId
        });
        if (res.success) {
            showToast('Application submitted successfully! 🎉', 'success');
            btn.textContent = '✅ Applied';
            btn.className = 'btn btn-secondary btn-sm';
        } else {
            showToast(res.message, 'error');
            btn.disabled = false;
            btn.textContent = '📩 Apply Now';
        }
    } catch (err) {
        showToast('Failed to apply', 'error');
        btn.disabled = false;
        btn.textContent = '📩 Apply Now';
    }
}

// Admin creates a new job
async function handleCreateJob(event) {
    event.preventDefault();

    var job = {
        title: document.getElementById('jobTitle').value,
        description: document.getElementById('jobDesc').value,
        company: document.getElementById('jobCompany').value,
        location: document.getElementById('jobLocation').value,
        salary: document.getElementById('jobSalary').value,
        createdBy: currentUser.id    // Admin's user ID
    };

    try {
        var res = await apiCall('/jobs', 'POST', job);
        if (res.success) {
            showToast('Job posted successfully!', 'success');
            event.target.reset();
            document.getElementById('jobLocation').value = 'Remote';
            loadJobs();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to create job', 'error');
    }
}

// Admin deletes a job
async function deleteJob(jobId) {
    if (!confirm('Are you sure you want to delete this job?')) return;

    try {
        var res = await apiCall('/jobs/' + jobId + '?adminId=' + currentUser.id, 'DELETE');
        if (res.success) {
            showToast('Job deleted', 'success');
            loadJobs();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to delete job', 'error');
    }
}

// ══════════════════════════════════════════════
// APPLICATIONS — Admin views and manages all applications
// ══════════════════════════════════════════════

async function loadApplications() {
    var container = document.getElementById('applicationsList');
    container.innerHTML = '<p>Loading applications...</p>';

    try {
        var res = await apiCall('/jobs/all-applications');
        if (res.success) {
            renderApplications(res.data);
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load applications</h3></div>';
    }
}

// Render applications table with status dropdown
function renderApplications(apps) {
    var container = document.getElementById('applicationsList');

    if (apps.length === 0) {
        container.innerHTML = '<div class="empty-state"><div class="empty-icon">📋</div>' +
            '<h3>No applications yet</h3><p>Students haven\'t applied for any jobs yet</p></div>';
        return;
    }

    var html = '<table class="results-table"><thead><tr>' +
        '<th>#</th><th>Student</th><th>Email</th><th>Job</th><th>Status</th><th>Action</th>' +
        '</tr></thead><tbody>';

    apps.forEach(function(app, i) {
        var statusClass = app.status === 'SELECTED' ? 'status-selected' :
                          app.status === 'REJECTED' ? 'status-rejected' : 'status-pending';

        html += '<tr>';
        html += '<td>' + (i + 1) + '</td>';
        html += '<td><b>' + (app.userName || 'N/A') + '</b></td>';
        html += '<td>' + (app.userEmail || 'N/A') + '</td>';
        html += '<td>' + (app.jobTitle || 'Job #' + app.jobId) + '</td>';
        html += '<td><span class="status-badge ' + statusClass + '">' + app.status + '</span></td>';
        html += '<td>';
        html += '<select onchange="updateAppStatus(' + app.id + ', this.value)" class="status-select">';
        html += '<option value="">— Update —</option>';
        html += '<option value="PENDING"' + (app.status === 'PENDING' ? ' selected' : '') + '>PENDING</option>';
        html += '<option value="SELECTED"' + (app.status === 'SELECTED' ? ' selected' : '') + '>SELECTED</option>';
        html += '<option value="REJECTED"' + (app.status === 'REJECTED' ? ' selected' : '') + '>REJECTED</option>';
        html += '</select>';
        html += '</td></tr>';
    });

    html += '</tbody></table>';
    container.innerHTML = html;
}

// Admin updates application status
async function updateAppStatus(appId, status) {
    if (!status) return;
    try {
        var res = await apiCall('/jobs/application-status', 'PUT', {
            applicationId: appId,
            status: status,
            adminId: currentUser.id
        });
        if (res.success) {
            showToast('Application status updated to ' + status, 'success');
            loadApplications();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to update status', 'error');
    }
}

// ══════════════════════════════════════════════
// TESTS — Admin: create tests / Student: take tests
// ══════════════════════════════════════════════

async function loadTests() {
    var isAdmin = currentUser.role === 'ADMIN';

    if (isAdmin) {
        // Show admin test section, hide student section
        document.getElementById('adminTestSection').classList.remove('hidden');
        document.getElementById('studentTestSection').classList.add('hidden');
        // Load dropdown data for test creation
        await loadTestDropdowns();
        await loadAllTests();
    } else {
        // Show student test section, hide admin section
        document.getElementById('adminTestSection').classList.add('hidden');
        document.getElementById('studentTestSection').classList.remove('hidden');
        document.getElementById('activeTest').classList.add('hidden');
        await loadAssignedTests();
    }
}

// Load dropdowns for admin test creation (jobs + students)
async function loadTestDropdowns() {
    try {
        // Load jobs for the "Link to Job" dropdown
        var jobsRes = await apiCall('/jobs');
        var jobSelect = document.getElementById('testJob');
        jobSelect.innerHTML = '<option value="">— No linked job —</option>';
        if (jobsRes.success) {
            jobsRes.data.forEach(function(job) {
                jobSelect.innerHTML += '<option value="' + job.id + '">' + job.title + ' - ' + job.company + '</option>';
            });
        }

        // Load students for the "Assign to Student" dropdown
        var studentsRes = await apiCall('/users/students');
        var studentSelect = document.getElementById('testStudent');
        studentSelect.innerHTML = '<option value="">— Select Student —</option>';
        if (studentsRes.success) {
            studentsRes.data.forEach(function(student) {
                studentSelect.innerHTML += '<option value="' + student.id + '">' + student.name + ' (' + student.email + ')</option>';
            });
        }
    } catch (err) {
        console.error('Failed to load dropdowns:', err);
    }
}

// Add a question field to the admin test creation form
function addQuestionField() {
    questionCount++;
    var container = document.getElementById('testQuestionsContainer');
    var div = document.createElement('div');
    div.className = 'question-block glass-card';
    div.id = 'questionBlock' + questionCount;
    div.innerHTML =
        '<div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">' +
        '<h4>Question ' + questionCount + '</h4>' +
        '<button type="button" class="btn btn-danger btn-sm" onclick="removeQuestionField(\'' + div.id + '\')">✕ Remove</button>' +
        '</div>' +
        '<div class="form-group">' +
        '<label>Question Text</label>' +
        '<input type="text" class="q-text" required placeholder="Enter question text">' +
        '</div>' +
        '<div style="display:grid; grid-template-columns: 1fr 1fr; gap:0.5rem;">' +
        '<div class="form-group"><label>Option A</label><input type="text" class="q-optA" required placeholder="Option A"></div>' +
        '<div class="form-group"><label>Option B</label><input type="text" class="q-optB" required placeholder="Option B"></div>' +
        '<div class="form-group"><label>Option C</label><input type="text" class="q-optC" required placeholder="Option C"></div>' +
        '<div class="form-group"><label>Option D</label><input type="text" class="q-optD" required placeholder="Option D"></div>' +
        '</div>' +
        '<div class="form-group">' +
        '<label>Correct Answer</label>' +
        '<select class="q-correct" required>' +
        '<option value="A">A</option><option value="B">B</option><option value="C">C</option><option value="D">D</option>' +
        '</select>' +
        '</div>';
    container.appendChild(div);
}

// Remove a question field
function removeQuestionField(blockId) {
    var block = document.getElementById(blockId);
    if (block) block.remove();
}

// Admin creates a test with MCQ questions
async function handleCreateTest(event) {
    event.preventDefault();

    var title = document.getElementById('testTitle').value;
    var jobId = document.getElementById('testJob').value || null;
    var assignedUserId = document.getElementById('testStudent').value;

    if (!assignedUserId) {
        showToast('Please select a student to assign the test', 'error');
        return;
    }

    // Collect all questions from dynamic form fields
    var questionBlocks = document.querySelectorAll('.question-block');
    if (questionBlocks.length === 0) {
        showToast('Please add at least one question', 'error');
        return;
    }

    var questions = [];
    var valid = true;
    questionBlocks.forEach(function(block) {
        var text = block.querySelector('.q-text').value;
        var optA = block.querySelector('.q-optA').value;
        var optB = block.querySelector('.q-optB').value;
        var optC = block.querySelector('.q-optC').value;
        var optD = block.querySelector('.q-optD').value;
        var correct = block.querySelector('.q-correct').value;

        if (!text || !optA || !optB || !optC || !optD) {
            valid = false;
            return;
        }

        questions.push({
            questionText: text,
            optionA: optA,
            optionB: optB,
            optionC: optC,
            optionD: optD,
            correctAnswer: correct
        });
    });

    if (!valid) {
        showToast('Please fill in all question fields', 'error');
        return;
    }

    try {
        var res = await apiCall('/test/create', 'POST', {
            title: title,
            jobId: jobId,
            assignedUserId: parseInt(assignedUserId),
            createdBy: currentUser.id,
            questions: questions
        });

        if (res.success) {
            showToast('Test created and assigned successfully!', 'success');
            document.getElementById('createTestForm').reset();
            document.getElementById('testQuestionsContainer').innerHTML = '';
            questionCount = 0;
            loadAllTests();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to create test', 'error');
    }
}

// Admin: Load all tests
async function loadAllTests() {
    var container = document.getElementById('allTestsList');
    try {
        var res = await apiCall('/test/all');
        if (res.success) {
            if (res.data.length === 0) {
                container.innerHTML = '<div class="empty-state"><p>No tests created yet</p></div>';
                return;
            }

            var html = '<table class="results-table"><thead><tr>' +
                '<th>#</th><th>Title</th><th>Assigned To</th><th>Status</th><th>Created</th>' +
                '</tr></thead><tbody>';

            res.data.forEach(function(test, i) {
                var statusClass = test.status === 'COMPLETED' ? 'status-selected' :
                                  test.status === 'IN_PROGRESS' ? 'status-pending' : 'status-pending';
                html += '<tr>';
                html += '<td>' + (i + 1) + '</td>';
                html += '<td><b>' + (test.title || 'Test #' + test.id) + '</b></td>';
                html += '<td>' + (test.assignedUserName || 'User #' + (test.assignedUserId || 'N/A')) + '</td>';
                html += '<td><span class="status-badge ' + statusClass + '">' + test.status + '</span></td>';
                html += '<td style="color:var(--text-muted); font-size:0.85rem;">' +
                    (test.startedAt ? new Date(test.startedAt).toLocaleDateString() : 'N/A') + '</td>';
                html += '</tr>';
            });

            html += '</tbody></table>';
            container.innerHTML = html;
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load tests</h3></div>';
    }
}

// Student: Load assigned tests
async function loadAssignedTests() {
    var container = document.getElementById('assignedTestsList');
    container.innerHTML = '<p>Loading your tests...</p>';

    try {
        var res = await apiCall('/test/assigned/' + currentUser.id);
        if (res.success) {
            if (res.data.length === 0) {
                container.innerHTML = '<div class="empty-state"><div class="empty-icon">📝</div>' +
                    '<h3>No tests assigned</h3><p>Your admin has not assigned any tests to you yet</p></div>';
                return;
            }

            var html = '';
            res.data.forEach(function(test) {
                var statusClass = test.status === 'COMPLETED' ? 'status-selected' :
                                  test.status === 'IN_PROGRESS' ? 'status-pending' : 'status-pending';
                html += '<div class="glass-card" style="margin-bottom:1rem;">';
                html += '<div style="display:flex; justify-content:space-between; align-items:center;">';
                html += '<div>';
                html += '<h3>' + (test.title || 'Assessment #' + test.id) + '</h3>';
                html += '<span class="status-badge ' + statusClass + '">' + test.status + '</span>';
                html += '</div>';

                // Only show "Start Test" for ASSIGNED or IN_PROGRESS tests
                if (test.status === 'ASSIGNED' || test.status === 'IN_PROGRESS') {
                    html += '<button class="btn btn-primary" onclick="startTest(' + test.id + ')">🚀 Start Test</button>';
                } else {
                    html += '<span style="color:var(--success);">✅ Completed</span>';
                }

                html += '</div></div>';
            });

            container.innerHTML = html;
        } else {
            container.innerHTML = '<div class="empty-state"><h3>' + res.message + '</h3></div>';
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load tests</h3></div>';
    }
}

// Student starts an assigned test
async function startTest(sessionId) {
    try {
        var res = await apiCall('/test/start', 'POST', {
            sessionId: sessionId,
            userId: currentUser.id
        });

        if (res.success) {
            testSession = res.data;
            testQuestions = testSession.questions;

            // Hide test list, show active test
            document.getElementById('assignedTestsList').classList.add('hidden');
            document.getElementById('activeTest').classList.remove('hidden');
            document.getElementById('activeTestTitle').textContent = testSession.title || 'Assessment';
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
}

// Render test questions (student is taking the test)
function renderQuestions() {
    var container = document.getElementById('questionsContainer');
    var html = '';

    testQuestions.forEach(function(q, i) {
        html += '<div class="question-card">';
        html += '<div class="question-number">' + (i + 1) + '</div>';
        html += '<div class="question-text">' + q.questionText + '</div>';
        html += '<div class="options-grid">';

        ['A', 'B', 'C', 'D'].forEach(function(opt) {
            html += '<label class="option-label" onclick="selectOption(' + q.id + ', \'' + opt + '\', this)">';
            html += '<input type="radio" name="q_' + q.id + '" value="' + opt + '">';
            html += '<span class="option-text">' + opt + ') ' + q['option' + opt] + '</span>';
            html += '</label>';
        });

        html += '</div></div>';
    });

    container.innerHTML = html;
    updateProgress();
}

// Mark selected option
function selectOption(questionId, option, element) {
    // Deselect previous selection for this question
    var siblings = document.querySelectorAll('label[onclick*="selectOption(' + questionId + '"]');
    siblings.forEach(function(el) { el.classList.remove('selected'); });
    // Mark current as selected
    element.classList.add('selected');
    updateProgress();
}

// Update progress bar
function updateProgress() {
    var uniqueAnswered = new Set();
    document.querySelectorAll('.option-label.selected').forEach(function(el) {
        var radioInput = el.querySelector('input[type="radio"]');
        if (radioInput) {
            var name = radioInput.name; // e.g. "q_5"
            uniqueAnswered.add(name);
        }
    });
    var total = testQuestions.length;
    var pct = total > 0 ? (uniqueAnswered.size / total) * 100 : 0;
    document.getElementById('progressFill').style.width = pct + '%';
    document.getElementById('currentQ').textContent = uniqueAnswered.size;
}

// Start countdown timer (10 minutes)
function startTimer() {
    timeLeft = 600;
    updateTimerDisplay();
    timerInterval = setInterval(function() {
        timeLeft--;
        updateTimerDisplay();
        if (timeLeft <= 0) {
            clearInterval(timerInterval);
            showToast('⏰ Time is up! Auto-submitting...', 'error');
            submitTest();
        }
    }, 1000);
}

// Update timer display
function updateTimerDisplay() {
    var minutes = Math.floor(timeLeft / 60);
    var seconds = timeLeft % 60;
    var display = '⏱ ' + String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
    var timerEl = document.getElementById('timerDisplay');
    timerEl.textContent = display;
    // Visual warnings when time is running out
    timerEl.className = 'timer';
    if (timeLeft <= 60) timerEl.classList.add('danger');
    else if (timeLeft <= 180) timerEl.classList.add('warning');
}

// Submit test answers
async function submitTest() {
    clearInterval(timerInterval);
    var btn = document.getElementById('submitTestBtn');
    btn.disabled = true;
    btn.textContent = 'Submitting...';

    // Collect all selected answers
    var answers = {};
    testQuestions.forEach(function(q) {
        var selected = document.querySelector('input[name="q_' + q.id + '"]:checked');
        if (selected) {
            answers[String(q.id)] = selected.value;
        }
    });

    try {
        var res = await apiCall('/test/submit', 'POST', {
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
            btn.textContent = '✅ Submit Test';
        }
    } catch (err) {
        showToast('Failed to submit test', 'error');
        btn.disabled = false;
        btn.textContent = '✅ Submit Test';
    }
}

// Display test result after submission
function displayResult(result) {
    // Switch to results view
    showView('results');

    // Show result display, hide history
    document.getElementById('resultDisplay').classList.remove('hidden');
    document.getElementById('allResultsSection').classList.add('hidden');

    // Populate values
    document.getElementById('resultScore').textContent = result.score;
    document.getElementById('resultTotal').textContent = result.totalQuestions;
    document.getElementById('resultCorrect').textContent = result.score;
    document.getElementById('resultWrong').textContent = result.totalQuestions - result.score;
    document.getElementById('resultPercentage').textContent = result.percentage + '%';

    // Message based on percentage
    var msgEl = document.getElementById('resultMessage');
    var pct = result.percentage;
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
}

// ══════════════════════════════════════════════
// RESULTS — View test results (role-specific)
// ══════════════════════════════════════════════

async function loadResults() {
    document.getElementById('resultDisplay').classList.add('hidden');
    document.getElementById('allResultsSection').classList.remove('hidden');

    var container = document.getElementById('allResultsList');
    var isAdmin = currentUser.role === 'ADMIN';

    // Update title based on role
    document.getElementById('resultsTitle').textContent = isAdmin ? '🏆 All Student Results' : '🏆 Your Results';
    document.getElementById('resultsSubtitle').textContent = isAdmin
        ? 'Review and evaluate student performance'
        : 'Track your assessment performance';

    try {
        // Admin sees ALL results, student sees only their own
        var endpoint = isAdmin ? '/results/all' : '/results/user/' + currentUser.id;
        var res = await apiCall(endpoint);

        if (res.success) {
            var results = res.data;

            if (results.length === 0) {
                container.innerHTML = '<div class="empty-state"><div class="empty-icon">🏆</div>' +
                    '<h3>No results yet</h3><p>' +
                    (isAdmin ? 'No students have completed tests yet' : 'Take an assessment to see your scores here') +
                    '</p></div>';
                return;
            }

            var html = '<table class="results-table"><thead><tr>';
            html += '<th>#</th>';
            if (isAdmin) html += '<th>Student</th><th>Email</th>';
            html += '<th>Test</th><th>Score</th><th>Total</th><th>Percentage</th><th>Status</th>';
            if (isAdmin) html += '<th>Action</th>';
            html += '<th>Date</th></tr></thead><tbody>';

            results.forEach(function(r, i) {
                var date = r.completedAt ? new Date(r.completedAt).toLocaleDateString('en-IN', {
                    day: '2-digit', month: 'short', year: 'numeric'
                }) : 'N/A';
                var pctColor = r.percentage >= 70 ? 'var(--success)' : r.percentage >= 40 ? 'var(--warning)' : 'var(--danger)';
                var statusClass = r.status === 'SELECTED' ? 'status-selected' :
                                  r.status === 'REJECTED' ? 'status-rejected' : 'status-pending';

                html += '<tr>';
                html += '<td>' + (i + 1) + '</td>';
                if (isAdmin) {
                    html += '<td><b>' + (r.userName || 'N/A') + '</b></td>';
                    html += '<td>' + (r.userEmail || 'N/A') + '</td>';
                }
                html += '<td>' + (r.testTitle || 'Test #' + r.testSessionId) + '</td>';
                html += '<td><b>' + r.score + '</b></td>';
                html += '<td>' + r.totalQuestions + '</td>';
                html += '<td style="color:' + pctColor + '; font-weight:700;">' + r.percentage + '%</td>';
                html += '<td><span class="status-badge ' + statusClass + '">' + r.status + '</span></td>';

                // Admin can update result status
                if (isAdmin) {
                    html += '<td>';
                    html += '<select onchange="updateResultStatus(' + r.id + ', this.value)" class="status-select">';
                    html += '<option value="">— Update —</option>';
                    html += '<option value="PENDING">PENDING</option>';
                    html += '<option value="SELECTED">SELECTED</option>';
                    html += '<option value="REJECTED">REJECTED</option>';
                    html += '</select></td>';
                }

                html += '<td style="color:var(--text-muted); font-size:0.85rem;">' + date + '</td>';
                html += '</tr>';
            });

            html += '</tbody></table>';
            container.innerHTML = html;
        }
    } catch (err) {
        container.innerHTML = '<div class="empty-state"><h3>Failed to load results</h3></div>';
    }
}

// Admin updates a result's status (SELECTED / REJECTED)
async function updateResultStatus(resultId, status) {
    if (!status) return;
    try {
        var res = await apiCall('/results/' + resultId + '/status', 'PUT', {
            status: status,
            adminId: currentUser.id
        });
        if (res.success) {
            showToast('Result status updated to ' + status, 'success');
            loadResults();
        } else {
            showToast(res.message, 'error');
        }
    } catch (err) {
        showToast('Failed to update status', 'error');
    }
}

// ══════════════════════════════════════════════
// INIT — Check for saved login session on page load
// ══════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', function() {
    var saved = localStorage.getItem('user');
    if (saved) {
        try {
            currentUser = JSON.parse(saved);
            showApp();
        } catch (e) {
            localStorage.removeItem('user');
        }
    }
});

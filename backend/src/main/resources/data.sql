-- =============================================
-- SEED DATA
-- =============================================

-- Default Admin User (password: admin123)
INSERT INTO users (name, email, password, role) VALUES ('Admin User', 'admin@jobportal.com', 'admin123', 'ADMIN');
INSERT INTO users (name, email, password, role) VALUES ('John Student', 'john@gmail.com', 'john123', 'STUDENT');

-- Sample Jobs
INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES ('Java Developer', 'Looking for an experienced Java developer with Spring Boot skills. Must have 2+ years of experience.', 'TechCorp India', 'Bangalore', '8-12 LPA', 1);
INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES ('Frontend Developer', 'Need a creative frontend developer proficient in React, HTML, CSS, and JavaScript.', 'WebSolutions', 'Hyderabad', '6-10 LPA', 1);
INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES ('Data Analyst', 'Seeking a data analyst with Python, SQL, and Tableau experience for data-driven insights.', 'DataMinds', 'Mumbai', '7-11 LPA', 1);
INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES ('DevOps Engineer', 'Looking for a DevOps engineer familiar with AWS, Docker, Kubernetes, and CI/CD pipelines.', 'CloudNine Tech', 'Pune', '10-15 LPA', 1);
INSERT INTO jobs (title, description, company, location, salary, created_by) VALUES ('Full Stack Developer', 'Full stack developer needed with Java backend and Angular/React frontend skills.', 'Infosys', 'Chennai', '9-14 LPA', 1);

-- 20 Sample Questions (General Aptitude + Technical)
INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What does JVM stand for?', 'Java Virtual Machine', 'Java Variable Method', 'Java Visual Machine', 'Java Verified Module', 'A');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which data structure uses FIFO?', 'Stack', 'Queue', 'Tree', 'Graph', 'B');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is the time complexity of binary search?', 'O(n)', 'O(n log n)', 'O(log n)', 'O(1)', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which keyword is used to inherit a class in Java?', 'implements', 'extends', 'inherits', 'super', 'B');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is the default value of a boolean in Java?', 'true', '0', 'null', 'false', 'D');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which SQL command is used to retrieve data?', 'GET', 'FETCH', 'SELECT', 'RETRIEVE', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What does HTML stand for?', 'Hyper Text Markup Language', 'High Tech Modern Language', 'Hyper Transfer Markup Logic', 'Home Tool Markup Language', 'A');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which protocol is used for secure web communication?', 'HTTP', 'FTP', 'HTTPS', 'SMTP', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is the size of int in Java?', '2 bytes', '4 bytes', '8 bytes', '16 bytes', 'B');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which design pattern ensures only one instance of a class?', 'Factory', 'Observer', 'Singleton', 'Strategy', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What does CSS stand for?', 'Creative Style Sheets', 'Cascading Style Sheets', 'Computer Style Sheets', 'Colorful Style Sheets', 'B');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which collection does not allow duplicate values?', 'List', 'ArrayList', 'Set', 'LinkedList', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is polymorphism in OOP?', 'One class inheriting another', 'Hiding implementation details', 'Same method behaving differently', 'Wrapping data and methods', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which HTTP method is used to update a resource?', 'GET', 'POST', 'PUT', 'DELETE', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is the main function of an operating system?', 'Compile code', 'Manage hardware resources', 'Browse the internet', 'Create databases', 'B');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What does REST stand for?', 'Representational State Transfer', 'Remote Execution Standard Technology', 'Real-time Event Streaming', 'Request-Execute-Store-Transfer', 'A');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which sorting algorithm has the best average-case time complexity?', 'Bubble Sort', 'Selection Sort', 'Merge Sort', 'Insertion Sort', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is a primary key in a database?', 'A key that can be null', 'A key that allows duplicates', 'A unique identifier for each record', 'A foreign reference', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('What is the purpose of the finally block in Java?', 'To catch exceptions', 'To throw exceptions', 'To execute code regardless of exceptions', 'To ignore exceptions', 'C');

INSERT INTO questions (question_text, option_a, option_b, option_c, option_d, correct_answer) VALUES
('Which layer of the OSI model handles routing?', 'Data Link Layer', 'Transport Layer', 'Network Layer', 'Application Layer', 'C');

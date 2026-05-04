require('dotenv').config();
const mysql = require('mysql2/promise');

const resumes = [
  {
    "fullName": "WISSEM FARHAT",
    "email": "wissem.farhat@eniso.u-sousse.tn",
    "phone": "21654722011",
    "skills": ["Python", "Linux", "YOLO", "OpenCV", "TensorFlow Lite", "Edge AI", "Model Deployment", "Computer Vision", "Server-Client Architecture", "Sensor Integration", "Scratch Programming", "MQTT", "Embedded Systems", "Image Classification", "Data Analysis", "Scikit-Learn", "C", "SQL", "Git", "Numpy", "IOT", "Image Processing", "Machine Learning", "Matplotlib", "Pandas", "Anaconda", "Neural Network"],
    "experience": ["Smart Greenhouse Solutions", "Design and Development of Educational Robot 'Clever' | Quetratech", "Researcher | Data scientist"],
    "education": ["Master's Degree in Advanced Electrical Engineering", "Bachelor's degree in industrial electronics"],
    "languages": ["Arabic", "French", "English"],
    "senderEmail": "wissemfarhat07@gmail.com",
    "emailSubject": "CV",
    "receivedAt": "2026-04-29T19:23:19.978Z"
  }
];

function convertISOToMySQLDateTime(isoString) {
  const date = new Date(isoString);
  const year = date.getUTCFullYear();
  const month = String(date.getUTCMonth() + 1).padStart(2, '0');
  const day = String(date.getUTCDate()).padStart(2, '0');
  const hours = String(date.getUTCHours()).padStart(2, '0');
  const minutes = String(date.getUTCMinutes()).padStart(2, '0');
  const seconds = String(date.getUTCSeconds()).padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

async function insertResumes() {
  let connection;
  try {
    connection = await mysql.createConnection({
      host: process.env.DB_HOST,
      port: process.env.DB_PORT,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME
    });

    for (const resume of resumes) {
      const transformedResume = {
        full_name: resume.fullName,
        email: resume.email,
        phone: resume.phone,
        skills: resume.skills.join(', '),
        experience: resume.experience.join(' | '),
        education: resume.education.join(' | '),
        languages: resume.languages.join(', '),
        sender_email: resume.senderEmail,
        email_subject: resume.emailSubject,
        received_at: convertISOToMySQLDateTime(resume.receivedAt)
      };

      const insertQuery = `
        INSERT INTO resumes (full_name, email, phone, skills, experience, education, languages, sender_email, email_subject, received_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
      `;

      const [result] = await connection.query(insertQuery, [
        transformedResume.full_name,
        transformedResume.email,
        transformedResume.phone,
        transformedResume.skills,
        transformedResume.experience,
        transformedResume.education,
        transformedResume.languages,
        transformedResume.sender_email,
        transformedResume.email_subject,
        transformedResume.received_at
      ]);

      console.log(`✓ Resume inserted with ID: ${result.insertId}`);
    }

    // Show last 5 rows
    const [rows] = await connection.query('SELECT * FROM resumes ORDER BY id DESC LIMIT 5');
    console.log('\n✓ Last 5 rows in resumes table:');
    console.table(rows);

    await connection.end();
  } catch (error) {
    console.error('✗ Failed to insert resume!');
    console.error(`Error: ${error.message}`);
    process.exit(1);
  }
}

insertResumes();

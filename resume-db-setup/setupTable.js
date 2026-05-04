require('dotenv').config();
const mysql = require('mysql2/promise');

async function setupTable() {
  let connection;
  try {
    connection = await mysql.createConnection({
      host: process.env.DB_HOST,
      port: process.env.DB_PORT,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME
    });

    const createTableQuery = `
      CREATE TABLE IF NOT EXISTS resumes (
        id INT AUTO_INCREMENT PRIMARY KEY,
        full_name VARCHAR(255),
        email VARCHAR(255),
        phone VARCHAR(50),
        skills TEXT,
        experience TEXT,
        education TEXT,
        languages VARCHAR(255),
        sender_email VARCHAR(255),
        email_subject VARCHAR(255),
        received_at DATETIME,
        created_at DATETIME DEFAULT NOW()
      ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    `;

    await connection.query(createTableQuery);
    console.log('✓ Table "resumes" created or already exists!');

    await connection.end();
  } catch (error) {
    console.error('✗ Failed to create table!');
    console.error(`Error: ${error.message}`);
    process.exit(1);
  }
}

setupTable();

require('dotenv').config();
const mysql = require('mysql2/promise');

async function testConnection() {
  try {
    const connection = await mysql.createConnection({
      host: process.env.DB_HOST,
      port: process.env.DB_PORT,
      user: process.env.DB_USER,
      password: process.env.DB_PASSWORD,
      database: process.env.DB_NAME
    });

    const [rows] = await connection.query('SELECT VERSION() as version');
    console.log('✓ MySQL Connection Successful!');
    console.log(`✓ MySQL Version: ${rows[0].version}`);
    
    await connection.end();
  } catch (error) {
    console.error('✗ MySQL Connection Failed!');
    console.error(`Error: ${error.message}\n`);
    console.error('Troubleshooting Tips:');
    console.error('1. Check if MySQL server is running');
    console.error('2. Verify DB_HOST is correct (default: localhost)');
    console.error('3. Verify DB_PORT is correct (default: 3306)');
    console.error('4. Verify DB_USER and DB_PASSWORD are correct');
    console.error('5. Verify DB_NAME database exists');
    console.error('6. Check .env file is properly configured');
    process.exit(1);
  }
}

testConnection();

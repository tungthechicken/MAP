const express = require('express');
const { MongoClient } = require('mongodb');
const cors = require('cors');
const bcrypt = require('bcrypt');
const nodemailer = require('nodemailer');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3000; // Use environment variable or default to 3000
const url = "mongodb+srv://pothole:pothole123@pothole-login.okpwh.mongodb.net/?retryWrites=true&w=majority&appName=Pothole-Login";
//const url = "mongodb://localhost:27017";
const dbName = "myDb";

// Middleware
app.use(cors());
app.use(express.json());

// Number of salt rounds for bcrypt
const saltRounds = 10;

// Configure nodemailer
const transporter = nodemailer.createTransport({
  service: 'Gmail',
  auth: {
    user: 'tungthechicken@gmail.com',
    pass: 'fewu jthi jkpz fsok'
  }
});

// MongoDB connection setup
let db;
async function main() {
  const client = new MongoClient(url, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    serverSelectionTimeoutMS: 10000  // Timeout after 10 seconds if no connection
  });

  try {
    await client.connect();
    console.log("Connected to MongoDB!");

    db = client.db(dbName);
    app.locals.potholeCollection = db.collection('potholes'); // Access pothole collection globally
    app.locals.userCollection = db.collection('myTable'); // Access user collection globally
  } catch (err) {
    console.error("Error connecting to MongoDB:", err);
    process.exit(1); // Exit if unable to connect
  }
}

// Call main function to initialize the database connection
main().then(() => {
  // Start the server only after MongoDB connection
  app.listen(PORT, () => {
    console.log(`Server is running on port ${PORT}`);
  });
}).catch(err => {
  console.error("Failed to start server:", err);
});

// POST endpoint to add a new pothole
app.post('/pothole', async (req, res) => {
  const collection = app.locals.potholeCollection;

  try {
    const result = await collection.insertOne(req.body);
    console.log("New pothole added:", result.insertedId);
    res.status(201).json({ _id: result.insertedId, ...req.body }); // Send back the inserted document
  } catch (error) {
    console.error("Error adding new pothole:", error);
    res.status(500).json({ message: "Database error" });
  }
});

// GET endpoint to fetch a pothole by location
app.get('/pothole', async (req, res) => {
  const collection = app.locals.potholeCollection;

  try {
    const potholes = await collection.find().toArray();
    
    if (potholes.length === 0) {
      return res.status(404).json({ message: "Không tìm thấy pothole" });
    }

    console.log("Fetched potholes:", potholes);
    res.json(potholes);
  } catch (error) {
    console.error("Error fetching potholes:", error);
    res.status(500).json({ message: "Lỗi khi truy vấn cơ sở dữ liệu" });
  }
});

// GET endpoint to fetch a pothole by location
app.get('/pothole/location', async (req, res) => {
  const { latitude, longitude } = req.query;  // Lấy tọa độ từ query string
  
  if (!latitude || !longitude) {
    return res.status(400).json({ message: "Latitude and Longitude are required" });
  }

  const collection = app.locals.potholeCollection;

  try {
    const potholes = await collection.find({
      "location.latitude": { $eq: latitude },
      "location.longitude": { $eq: longitude }
    }).toArray();

    if (potholes.length === 0) {
      return res.status(404).json({ message: "Không tìm thấy pothole" });
    }

    console.log("Fetched potholes:", potholes);
    res.json(potholes);
  } catch (error) {
    console.error("Error fetching potholes:", error);
    res.status(500).json({ message: "Lỗi khi truy vấn cơ sở dữ liệu" });
  }
});

// PUT endpoint to update a pothole by ID
app.put('/pothole/:id', async (req, res) => {
  const collection = app.locals.potholeCollection;
  const { id } = req.params;

  try {
    const result = await collection.updateOne(
      { _id: new MongoClient.ObjectId(id) },
      { $set: req.body }
    );
    if (result.matchedCount === 0) {
      return res.status(404).json({ message: "Pothole not found" });
    }
    console.log("Updated pothole:", result);
    res.json({ message: 'Successfully updated!', updatedCount: result.modifiedCount });
  } catch (error) {
    console.error("Error updating pothole:", error);
    res.status(500).json({ message: "Database error" });
  }
});

// DELETE endpoint to delete a pothole by ID
app.delete('/pothole/:id', async (req, res) => {
  const collection = app.locals.potholeCollection;
  const { id } = req.params;

  try {
    const result = await collection.deleteOne({ _id: new MongoClient.ObjectId(id) });
    if (result.deletedCount === 0) {
      return res.status(404).json({ message: "Pothole not found" });
    }
    console.log("Deleted pothole with id:", id);
    res.json({ message: 'Successfully deleted!' });
  } catch (error) {
    console.error("Error deleting pothole:", error);
    res.status(500).json({ message: "Database error" });
  }
});

// Signup endpoint
app.post('/signup', async (req, res) => {
  const collection = app.locals.userCollection;

  try {
    console.log("Received signup request for:", req.body.email);
    const existingUser = await collection.findOne({ email: req.body.email });

    if (!existingUser) {
      const hashedPassword = req.body.password ? await bcrypt.hash(req.body.password, saltRounds) : null;

      const newUser = {
        email: req.body.email,
        name: req.body.name,
        password: hashedPassword,
        otp: null,
        otpExpires: null,
        otpVerified: false,
        isLinkedGoogle: req.body.isLinkedGoogle === "true" // Compare the request body value, should be false in database
      };

      await collection.insertOne(newUser);
      console.log("User signed up successfully:", newUser);
      res.status(200).send("Signup successful");
    } else {
      console.log("User already exists:", req.body.email);
      res.status(400).send("User already registered");
    }
  } catch (err) {
    console.error("Error in signup:", err);
    res.status(500).send("Database error");
  }
});

// Login endpoint
app.post('/login', async (req, res) => {
  const collection = app.locals.userCollection;

  console.log("Received login request for:", req.body.email);
  try {
    const user = await collection.findOne({ email: req.body.email });

    if (user) {
      const match = await bcrypt.compare(req.body.password, user.password);

      if (match) {
        const objToSend = {
          name: user.name,
          email: user.email
        };
        console.log("User logged in successfully:", objToSend);
        res.status(200).json(objToSend);
      } else {
        console.log("Login failed: incorrect password");
        res.status(404).send("Incorrect credentials");
      }
    } else {
      console.log("Login failed: user not found");
      res.status(404).send("Incorrect credentials");
    }
  } catch (err) {
    console.error("Error in login:", err);
    res.status(500).send("Database error");
  }
});

// Endpoint to send user data
app.post('/send-user-data', async (req, res) => {
  const collection = app.locals.userCollection;

  try {
    const { email, name, isLinkedGoogle } = req.body;
    const existingUser = await collection.findOne({ email });

    if (existingUser) {
      // Update existing user to link with Google
      await collection.updateOne(
        { email },
        { $set: { isLinkedGoogle: isLinkedGoogle === "true" } }
      );
      console.log("User linked with Google:", email);
      res.status(200).json(existingUser);
    } else {
      // Create new user with the specified structure
      const newUser = {
        email,
        name,
        password: null,
        otp: null,
        otpExpires: null,
        otpVerified: false,
        isLinkedGoogle: isLinkedGoogle === "true"
      };
      await collection.insertOne(newUser);
      console.log("New user created and linked with Google:", newUser);
      res.status(201).json(newUser);
    }
  } catch (err) {
    console.error("Error in send-user-data:", err);
    res.status(500).send("Database error");
  }
});

// Endpoint to GET user data by email
app.get('/get-user-data', async (req, res) => {
  const { email } = req.query;

  if (!email) {
      return res.status(400).json({ message: "Email is required" });
  }

  const collection = app.locals.userCollection;

  try {
      const user = await collection.findOne({ email });

      if (!user) {
          return res.status(404).json({ message: "User not found" });
      }

      res.json({ name: user.name, email: user.email });
  } catch (error) {
      console.error("Error fetching user:", error);
      res.status(500).json({ message: "Database error" });
  }
});

// Endpoint to set password
app.post('/set-password', async (req, res) => {
  const collection = app.locals.userCollection;
  const { email, password } = req.body;

  try {
    const hashedPassword = await bcrypt.hash(password, saltRounds);
    await collection.updateOne(
      { email },
      { $set: { password: hashedPassword } }
    );
    console.log("Password set successfully for:", email);
    res.status(200).send("Password set successfully");
  } catch (err) {
    console.error("Error setting password:", err);
    res.status(500).send("Database error");
  }
});

// Forgot password endpoint
app.post('/forgot-password', async (req, res) => {
  const collection = app.locals.userCollection;
  const { email } = req.body;
  console.log("Received forgot password request for:", email);

  try {
    const user = await collection.findOne({ email });

    if (!user) {
      return res.status(404).send("User not found");
    }

    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpExpiry = Date.now() + 180000;

    const hashedOTP = await bcrypt.hash(otp, saltRounds);

    await collection.updateOne(
      { email },
      { $set: { otp: hashedOTP, otpExpires: otpExpiry } }
    );

    const mailOptions = {
      to: email,
      from: 'your-email@gmail.com',
      subject: 'Password Reset OTP',
      text: `Your OTP for password reset is: ${otp}. It is valid for 3 minutes.`
    };

    transporter.sendMail(mailOptions, (err, response) => {
      if (err) {
        console.error('Error sending email:', err);
        return res.status(500).send("Error sending email");
      }
      res.status(200).send("OTP sent");
    });
  } catch (err) {
    console.error("Error in forgot-password:", err);
    res.status(500).send("Database error");
  }
});

// Verify OTP endpoint
app.post('/verify-otp', async (req, res) => {
  const collection = app.locals.userCollection;
  const { email, otp } = req.body;
  console.log("Received OTP verification request for:", email);

  try {
    const user = await collection.findOne({ email, otpExpires: { $gt: Date.now() } });

    if (!user) {
      return res.status(400).send("No valid user found");
    }

    const isMatch = await bcrypt.compare(otp, user.otp);

    if (!isMatch) {
      return res.status(400).send("Invalid OTP");
    }

    await collection.updateOne(
      { email },
      { $set: { otpVerified: true, otp: null, otpExpires: null } }
    );

    console.log("OTP verified successfully for:", email);
    res.status(200).send("OTP verified");
  } catch (err) {
    console.error("Error in verify-otp:", err);
    res.status(500).send("Database error");
  }
});

// Reset password endpoint
app.post('/reset-password', async (req, res) => {
  const collection = app.locals.userCollection;
  const { email, newPassword } = req.body;
  console.log(`Password reset attempt for: ${email}`);

  try {
    const user = await collection.findOne({ email, otpVerified: true });

    if (!user) {
      return res.status(400).send("OTP not verified or expired");
    }

    const hashedPassword = await bcrypt.hash(newPassword, saltRounds);

    await collection.updateOne(
      { email },
      { $set: { password: hashedPassword, otpVerified: false } }
    );

    console.log(`Password successfully reset for user: ${email}`);
    res.status(200).send("Password has been reset");
  } catch (err) {
    console.error("Error in reset-password:", err);
    res.status(500).send("Database error");
  }
});
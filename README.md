# 🏠 Smart Home Management System (Java + OOP Principles)

A fully object-oriented **Smart Home Device Management System** developed in Java.  
Supports lamps, robot vacuums, battery management, scheduled tasks, logging, and JSON/TXT save–load features.

---

## 🚀 Features

### ✔ Device Types
- **SmartLamp**
  - Adjustable brightness
  - Power ON / OFF
  - Battery tracking
- **RobotVacuum**
  - Modes: `eco`, `turbo`, `silent`
  - Auto battery drain on start
  - Auto shut-off when battery is low

### ✔ Core Functionalities
- Add new devices  
- Turn **all devices ON/OFF**  
- Search device by name  
- Change lamp brightness  
- Change robot vacuum mode  
- Show **all devices**  
- Show **low-battery devices**  
- **Log system** (timestamped events)  
- **Save / Load devices** to `.txt`  
- **Schedule auto turn-off** with timer  

---

## 🛠 Tech Stack
- **Java 17+**
- Object-Oriented Programming (OOP)
- Encapsulation, Inheritance, Polymorphism
- File I/O (TXT)
- Timer & TimerTask
- Clean code + English UI

---

## 📦 How to Run

```bash
git clone https://github.com/<your-username>/smartHome1.0-java.git
cd smartHome1.0-java
javac src/com/emreacr/smartHome/smartHome.java
java com.emreacr.smartHome.smartHome

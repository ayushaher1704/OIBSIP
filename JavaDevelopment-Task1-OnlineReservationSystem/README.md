# Online Reservation System — Java Development (Task 1)

A desktop Railway Reservation System built with **Java Swing** and **SQLite (JDBC)** for the Oasis Infobyte (OIBSIP) Java Development Internship.

---

## Key Features

- **Role-Based Authentication:** Sign-in portal (`LoginFrame`) connected to the main reservation dashboard (`MainFrame`) with session sign-out.
- **28 Train Routes & Intermediate Stops:** Pre-seeded SQLite catalog covering Rajdhani, Shatabdi, Duronto, Vande Bharat Express, and Konkan Railway corridors with ordered intermediate stations.
- **Train-Specific Coach Classes & Dynamic Fare (`₹`):** Loads only the coach classes operated by the selected train (`1A`, `2A`, `3A`, `SL`, `CC`, `EC`, `2S`) and calculates ticket fares live from stop distance, coach tier, and train category.
- **Modal Calendar Date Picker:** Interactive date selector (`DatePickerDialog`) with month navigation and automatic disabling of past dates.
- **Collision-Checked 10-Digit PNR Generation:** Generates unique 10-digit PNR numbers via `ThreadLocalRandom` and stores reservations persistently in SQLite.
- **Ticket Cancellation with Confirmation:** Fetches full reservation details by PNR and requires explicit confirmation before removing the ticket from the database.
- **Searchable All-Tickets Directory:** Live-filtered `JTable` listing all active bookings with one-click PNR copy to the system clipboard.
- **Light & Dark Theme Toggle:** Global `ThemeManager` applying consistent styling across all windows, cards, tables, and dialogs.
- **One-Click Windows Launcher:** Includes `run.bat` to compile, run, and clean up temporary `.class` build files automatically.

---

## Tech Stack

- **Language:** Java (JDK 17+)
- **GUI Framework:** Java Swing & AWT (`GridBagLayout`, `JTabbedPane`, `JTable`)
- **Database:** SQLite (`sqlite-jdbc`)

---

## Project Structure

```text
JavaDevelopment-Task1-OnlineReservationSystem/
├── data/
│   └── reservations.db          # Auto-generated SQLite database
├── lib/
│   └── sqlite-jdbc.jar          # SQLite JDBC driver
├── src/
│   ├── AuthService.java         # User login verification
│   ├── DatabaseManager.java     # Schema setup & 28-train catalog seeding
│   ├── DatePickerDialog.java    # Modal calendar date picker
│   ├── LoginFrame.java          # Sign-in window
│   ├── Main.java                # Application entry point
│   ├── MainFrame.java           # Book Ticket, Cancel Ticket & All Tickets UI
│   ├── Reservation.java         # Reservation model
│   ├── ReservationService.java  # PNR generation, fare logic & SQLite CRUD
│   └── ThemeManager.java        # Light/Dark UI theme engine
├── run.bat                      # One-click Windows build & run script
└── README.md
```

---

## Demo Login Credentials

| Role | Username | Password |
| :--- | :--- | :--- |
| Reservation Clerk | `user1` | `pass123` |
| Administrator | `admin` | `admin123` |

---

## How to Run

### Option 1: One-Click Launcher (Windows)
Double-click `run.bat` in File Explorer, or run from PowerShell:
```powershell
.\run.bat
```

### Option 2: Manual Compile & Run
```powershell
javac -d src src\*.java
java -cp "src;lib\sqlite-jdbc.jar" Main
```
# Task 3 — NexaBank Digital ATM Portal

A web-enabled ATM banking application built with Core Java (`com.sun.net.httpserver.HttpServer`) and a responsive HTML/CSS/JS frontend, containerized with Docker and deployed on Render.

## Live Demo
- **URL:** https://oibsip-wekr.onrender.com/
- **Sandbox Account 1:** User ID `1001` | PIN `1234`
- **Sandbox Account 2:** User ID `1002` | PIN `5678`

*(Note: Hosted on Render's free tier—the first request after 15 minutes of inactivity may take ~30–50 seconds to wake the container.)*

## Features
- **Account Authentication:** PIN verification with session state handling.
- **Deposit, Withdraw & Inter-Account Transfer:** Real-time balance updates across accounts.
- **Transaction & Balance Guardrails:** Enforces a ₹5,00,000 single-transaction limit and a ₹50,00,000 maximum account cap with live capacity tracking.
- **Statement History & Theme Switcher:** Timestamped credit/debit activity log and persistent Light/Dark mode.
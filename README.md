# TelegramBot using Gemini API

This project is a simple Telegram bot built using Spring Boot and Telegram Bot API, which interacts with the **Gemini API** for generating content. The bot processes user input, sends requests to Gemini API, and returns responses to the user through Telegram.

## Features

- The bot listens for messages in a Telegram chat.
- If a message starts with `bot`, the bot will process the text, send it to the Gemini API, and return a generated response.
- The bot uses **MarkdownV2** formatting for the responses.
- It handles API rate limits and retries up to 3 times if the rate limit is exceeded.

## Requirements

Before running the project, make sure you have:

- **Java 17** (or higher) installed.
- **Maven** installed.
- A **Telegram bot token**.
- **Gemini API key** from Google.

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/telegram-bot.git
cd telegram-bot
```

### 2. Generate geminiApikey

[Google Gemini API key](https://ai.google.dev/gemini-api/docs/api-key)

### 3. Create telegram bot and generate token using @BotFather

Insert these values into the application.properties.

### 4. Run the application in the IDE.

### 5. Attention
- The application.properties file contains keys that are forbidden in production!
- Logging to the IDE console is not added!
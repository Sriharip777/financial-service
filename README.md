# Financial Service - Tutoring Platform

Complete payment, payout, and refund management system for the tutoring platform.

## Features

### Payment Processing
- **Multiple Payment Gateways**: Stripe and Razorpay integration
- **Payment Methods**: Credit/Debit cards, UPI, Net Banking, Wallets
- **Installment Support**: Split payments into multiple installments
- **Commission Calculation**: Automatic platform commission (15% non-recurring, 10% recurring)
- **Webhook Support**: Real-time payment status updates

### Payout Management
- **Automated Payouts**: Weekly payout processing every Monday
- **Earnings Tracking**: Real-time teacher earnings calculation
- **Bank Transfer**: Direct bank transfer via IMPS/NEFT/RTGS
- **Minimum Threshold**: Configurable minimum payout amount (₹1000)

### Refund Processing
- **Policy-Based Refunds**: Full refund if cancelled >24 hours before class
- **Partial Refunds**: Configurable processing fees
- **Automated Processing**: Refunds processed back to original payment method

### Transaction Management
- **Complete Audit Trail**: All financial transactions logged
- **Transaction History**: Student and teacher transaction views
- **Real-time Updates**: Kafka event-driven architecture

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **MongoDB** (Database)
- **Apache Kafka** (Event Streaming)
- **Redis** (Caching)
- **Stripe SDK 24.4.0**
- **Razorpay SDK 1.4.5**

## Prerequisites

- JDK 17+
- Maven 3.8+
- MongoDB 7.0+
- Docker & Docker Compose
- Stripe Account (test keys)
- Razorpay Account (test keys)

## Environment Variables

Create `.env` file:

```env
# Stripe Configuration
STRIPE_SECRET_KEY=sk_test_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Razorpay Configuration
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_key_secret
RAZORPAY_WEBHOOK_SECRET=your_webhook_secret

# MongoDB
MONGODB_URI=mongodb://localhost:27017/tutoring_financial_db

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

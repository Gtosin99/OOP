# Project Requirements

## Project Name

Sante Laboratory Information Management System

## Goal

Build a JavaFX desktop application that helps a laboratory manage customers, test requests, samples, results, staff roles, email verification, and audit history.

## Core Requirements

- Users can register, log in, verify email, and change passwords.
- Admin users can manage roles, users, and test types.
- Customers can request laboratory tests and track request progress.
- Lab staff can manage samples and update sample status.
- Results can be uploaded, validated, and linked to test requests.
- Important actions must be written to audit logs.

## Non-Functional Requirements

- Use JavaFX for the user interface.
- Use PostgreSQL for persistent storage.
- Use Maven for dependency management.
- Use BCrypt for password hashing.
- Use Jakarta Mail for email workflows.
- Store result PDF files in `uploads/pdfs`.
- Store image files in `uploads/images`.

## Roles

- Admin
- Lab Scientist
- Customer

## Shared Rule

All team members must use the database schema, package names, and utility classes committed to `main`.

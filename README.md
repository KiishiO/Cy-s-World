# Cy's World 🏫

**A comprehensive campus application for Iowa State University**

*Developed for SE 3090: Software Development Practices - Spring 2025*

## 📖 Overview

Cy's World is an Android application designed to enhance the campus experience for Iowa State University students, teachers, and administrators. The app provides a centralized platform for academic management, social connections, campus resources, and real-time communication.

## 👥 Team Members

**Group 2_swarna_4**
- **Jawad** - 25% contribution
- **Kiishi** - 25% contribution  
- **Jayden** - 25% contribution
- **Sonia Patil** - 25% contribution

## 🏆 Accomplishment Shoutout
Won the Best Heart Award for outstanding team collaboration and project impact.

## ✨ Key Features

### 🎓 For Students
- **Academic Dashboard**: View grades, GPA tracking, and class schedules
- **Social Features**: Add friends and create study groups
- **Real-time Chat**: Communicate with friends through WebSocket messaging
- **Campus Resources**: Access dining hall menus, bus routes, and event calendars

### 👨‍🏫 For Teachers  
- **Class Management**: Organize courses and student enrollment
- **Grade Management**: Input and update student grades in real-time
- **Campus Access**: View dining, transportation, and event information

### 👨‍💼 For Administrators
- **User Management**: Oversee student and teacher accounts
- **Class Setup**: Create and assign classes to users
- **Dining Management**: Update menus and dining hall information
- **Event Management**: Manage campus events and activities

## 🏗️ System Architecture

### Technology Stack
- **Frontend**: Android (Java/Kotlin)
- **Backend**: Spring Boot with RESTful APIs
- **Database**: MySQL with relational design
- **Real-time Features**: WebSocket implementation
- **Documentation**: Swagger UI

### Architecture Pattern
- **Client-Server Architecture** with role-based access control
- **Three-tier structure**: Frontend → Backend → Database
- **Modular design** with separated controllers, services, and repositories

## 🗄️ Database Design

The application uses a normalized relational database with the following core entities:
- **Users & Profiles**: Authentication and personal information
- **Academic Data**: Classes, grades, and enrollments  
- **Campus Resources**: Dining halls, bus routes, events
- **Social Features**: Friend connections and study groups
- **Communication**: Chat history and notifications

## 🚀 API Documentation

Access the Swagger documentation at:
```
http://coms-3090-017.las.iastate.edu:8080/swagger-ui/index.html
```

### Core API Endpoints
- **POST**: Create new objects in the database
- **GET**: Retrieve information by ID
- **PUT**: Update existing records without creating new objects
- **DELETE**: Remove objects from the database

## 📱 User Interface

The app features role-based dashboards with:
- **Consistent Iowa State branding** (cardinal red color scheme)
- **Responsive design** across various Android screen sizes
- **Intuitive navigation** with grid-based card layouts
- **Real-time updates** for dynamic content

### Screen Flow
```
Login Screen → Role-Based Dashboard → Feature-Specific Screens
    ├── Student Dashboard → Grades, Friends, Chat, Dining, Bus Routes
    ├── Teacher Dashboard → Class Management, Grade Management, Resources  
    └── Admin Dashboard → User Management, System Administration
```

## ⚡ Performance Requirements

- **Concurrent Users**: Minimum 5 active users (scalable to campus-wide)
- **Response Times**: 
  - UI interactions: < 0.5 seconds
  - Login operations: < 5 seconds
- **Real-time Features**: Instant messaging and live updates
- **Cross-platform**: Consistent experience across Android devices

## 🛠️ Development Setup

### Prerequisites
- Android Studio
- Java Development Kit (JDK)
- MySQL Database
- Spring Boot framework

### Installation
1. Clone the repository
```bash
git clone https://github.com/kiishiO/Cy-s-world.git
```

2. Set up the database using the provided schema

3. Configure application properties for database connection

4. Build and run the Spring Boot backend

5. Install and run the Android application

## 📋 Project Management

This project was developed using agile methodologies over the course of Spring 2025 semester, incorporating:
- **Version Control**: GitLab for development, GitHub for portfolio
- **Documentation**: Comprehensive design documents and API documentation
- **Testing**: Multi-user testing and performance validation
- **Code Quality**: Modular architecture with separation of concerns

## 🎯 Learning Outcomes

Through this project, our team gained experience in:
- **Full-stack mobile development**
- **Database design and implementation**
- **RESTful API development** 
- **Real-time communication systems**
- **Collaborative software development**
- **User interface design principles**

## 📸 Demo

View demo video here --> (https://youtu.be/u3hlZZZqzDs?si=wqZq28XTUW7NKTZV)

## 🔮 Future Enhancements

- Campus-wide deployment
- Integration with Iowa State's existing systems
- Enhanced analytics and reporting
- Mobile push notifications
- Offline functionality

## 📄 License

This project was developed as part of coursework for SE 3090 at Iowa State University.

---

*Developed with ❤️ by Group 2_swarna_4 for Iowa State University*

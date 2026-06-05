# API Contracts

These contracts describe the shared service methods the team should build toward. They are not web APIs; they are Java service-layer contracts for the JavaFX application.

## AuthService

```java
User login(String email, String password);
User registerCustomer(String fullName, String email, String password);
void sendVerificationEmail(int userId);
boolean verifyEmail(String token);
void changePassword(int userId, String oldPassword, String newPassword);
```

## UserService

```java
List<User> findAllUsers();
User findUserById(int id);
void createUser(User user);
void updateUser(User user);
void deactivateUser(int id);
```

## TestTypeService

```java
List<TestType> findAllActiveTestTypes();
TestType findTestTypeById(int id);
void createTestType(TestType testType);
void updateTestType(TestType testType);
```

## TestRequestService

```java
TestRequest createRequest(int customerId, int testTypeId);
List<TestRequest> findRequestsByCustomer(int customerId);
List<TestRequest> findAllRequests();
void updateRequestStatus(int requestId, String status);
```

## SampleService

```java
Sample createSample(int testRequestId);
void updateSampleStatus(int sampleId, String status, int updatedBy);
List<SampleStatusHistory> findStatusHistory(int sampleId);
```

## ResultService

```java
Result uploadResult(int testRequestId, String filePath, String resultType, int uploadedBy);
void validateResult(int resultId, int validatedBy);
List<Result> findResultsByCustomer(int customerId);
```

## AuditLogService

```java
void logAction(int userId, String action, String description);
List<AuditLog> findAllLogs();
```

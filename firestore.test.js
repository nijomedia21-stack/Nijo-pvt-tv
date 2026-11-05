const { initializeTestEnvironment, assertFails, assertSucceeds } = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

const PROJECT_ID = "demo-no-project";
let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8085
    }
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated user cannot read tasks", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("users/user1/tasks").get());
});

test("Authenticated user can create a valid task in their own collection", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("users/alice/tasks").add({
      userId: "alice",
      title: "Maths I Assignment 1",
      status: "TODO",
      workspace: "study",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Cross-user access is denied: Bob cannot read Alice's tasks", async () => {
  const bobDb = testEnv.authenticatedContext("bob").firestore();
  await assertFails(bobDb.collection("users/alice/tasks").get());
});

test("Invalid task status is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertFails(
    aliceDb.collection("users/alice/tasks").add({
      userId: "alice",
      title: "Invalid status task",
      status: "RANDOM_STATUS",
      workspace: "study",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

test("Authenticated user can save settings with 4-digit PIN", async () => {
  const aliceDb = testEnv.authenticatedContext("alice").firestore();
  await assertSucceeds(
    aliceDb.collection("users/alice/settings").doc("config").set({
      userId: "alice",
      securityPin: "8492",
      currentWorkspace: "study",
      createdAt: new Date(),
      updatedAt: new Date()
    })
  );
});

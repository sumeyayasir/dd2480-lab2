# DD2480: Assignment 2 — Continuous Integration Server

A small CI server that listens for GitHub push webhooks, clones the pushed branch, compiles the project, runs its tests, and reports the result back as a GitHub commit status. Built in collaboration with fellow course members.

## Architecture

When a push event occurs on GitHub, a webhook sends a JSON payload to our server's `/webhook` endpoint. The server:

1. **Parses** the payload (`WebhookPayload`) to extract the repository URL, branch name, and commit SHA.
2. **Sets a pending** commit status on GitHub via the REST API (`GitHubStatusNotifier`).
3. **Clones** the repository and runs `mvn compile` followed by `mvn test` (`BuildProcessor`).
4. **Reports** the final result (success/failure/error) as a commit status on GitHub.

The result is stored in a `CIResultObject` that tracks build success, test success, error messages, and the full build log.

## Requirements

- Java 21+
- Maven 3.9+
- Git
- A GitHub personal access token with `repo:status` scope (for commit status notification)

## Dependencies

| Dependency                               | Version  | Purpose                            |
| ---------------------------------------- | -------- | ---------------------------------- |
| `org.json:json`                          | 20240303 | Parse GitHub webhook JSON payloads |
| `org.junit.jupiter:junit-jupiter-api`    | 5.11.0   | Unit testing                       |
| `org.junit.jupiter:junit-jupiter-params` | 5.11.0   | Parameterized test support         |
| `org.mockito:mockito-core`               | 5.7.0    | Mocking in unit testing            |

## Build and Run

### Compile

```bash
mvn compile
```

### Run tests

```bash
mvn test
```

### Package and run the server

```bash
mvn package
GITHUB_TOKEN=ghp_your_token_here java -jar target/dd2480-ci-server-1.0-SNAPSHOT.jar
```

The server starts on port **8001** (convention: 8000 + group number).

### Generate Javadoc

```bash
mvn javadoc:javadoc
open docs/javadoc/index.html
```

### Expose to the internet (ngrok)

```bash
ngrok http 8001
```

Then configure the ngrok URL as a GitHub webhook pointing to `/webhook`.

## CI Feature Implementation

### P1 — Compilation

**Implementation:** `BuildProcessor.runBuild()` clones the repository at the specified branch into a temporary directory and runs `mvn compile`. The exit code determines whether compilation succeeded. The result is stored in `CIResultObject.setBuildSuccessful()`.

**How the branch is selected:** `WebhookPayload` parses the `ref` field from the GitHub push event (e.g. `refs/heads/feature/my-branch` → `feature/my-branch`) and passes it to `BuildProcessor`, which clones with `git clone -b <branch>`.

**Unit tests:** `BuildProcessorTest` uses a `TestBuildProcessor` subclass that overrides `runBuild()` to simulate clone/compile success and failure without executing real processes. Tests verify that the `CIResultObject` is correctly populated for all scenarios.

### P2 — Testing

**Implementation:** After a successful compilation, `BuildProcessor.runBuild()` runs `mvn test` in the cloned repository. The exit code determines whether tests passed. If compilation fails, test execution is skipped. The result is stored in `CIResultObject.setTestsSuccessful()`.

**Unit tests:** `BuildProcessorTest` includes tests for: successful build + tests, clone failure, compile failure (skips tests), compile success + test failure. Each test verifies the correct state in `CIResultObject`.

### P3 — Notification

**Implementation:** `GitHubStatusNotifier` sends HTTP POST requests to the GitHub commit status API (`/repos/{owner}/{repo}/statuses/{sha}`). It sets:

- **pending** before the build starts
- **success** if both compilation and tests pass
- **failure** if compilation or tests fail
- **error** if an exception occurs

Screenshots of working github notification with checkmarks for the user to see the status of the commit.
<img width="1384" height="489" alt="Skärmavbild 2026-02-12 kl  11 35 56" src="https://github.com/user-attachments/assets/04da2b49-4906-44b8-b3a0-cf645424ea28" />
<img width="1312" height="831" alt="Skärmavbild 2026-02-12 kl  11 34 20" src="https://github.com/user-attachments/assets/54f71495-1d8a-4c4f-8ac7-6626b2dc98ef" />


### P7 — Build History

**Implementation:** The server provides a persistent history of all build through a

- **Persistence:** `Server.saveBuildResult()` saves each CI outcome as a JSON file in the `build_history/` directory, including commit SHA, branch, date, and full execution logs.
- **Web Interface:** The `/builds` endpoint provides a dynamic HTML dashboard that lists past builds. Each entry links to a detailed view of the build's metadata and logs using query parameters.
  web interface.
  How to browse:
  Navigate to http://localhost:8001/builds in a web broser while the server is running.

**Unit tests:** BuildHistoryTest verifies that the build_history/ directory is automatically managed and that JSON serialization of build results is accurate and retrievable.

The GitHub token is read from the `GITHUB_TOKEN` environment variable.

**Unit tests:** `GitHubStatusNotifierTest` tests `mapResultToState()` and `buildDescription()` for all result combinations (success, build failure, test failure, exception) without making real HTTP calls.

### P8 — Discord Notification
The DiscordNotifier class is able to send real-time build status updates to a configured Discord server using webhook integrations
So when a build is complete (success or failure), the server constructs a JSON payload containing the build status, branch name, and message. It looks for the Discord Webhook URL and sends the payload via an HTTP request to it correspondingly.

## API Documentation

Generated Javadoc is available in [`docs/javadoc/`](docs/javadoc/index.html). All public classes and methods are documented.

## P6 SEMAT Team Assessment

We consider our team to be in the **"Seeded"** state. The team mission has been defined — build a CI server that handles compilation, testing, and notification — and individual responsibilities have been assigned. Constraints on the team's operation are known (deadline, tooling, port convention), the composition is defined (5 members), and governance rules are in place (feature branches, PRs with review, commit conventions).

However, we have not fully reached the "Formed" state. While individual responsibilities are understood and members are accepting work, not all team members have contributed equally, and some members have not yet fully engaged with the workflow (issues → branch → PR → review → merge). To reach "Formed", every member needs to be actively accepting and completing work, and the team communication needs to be more consistent. To reach "Collaborating", the team would need to function as one cohesive unit with open communication and mutual trust, which requires more time working together.

## Statement of Contributions

- **Sumeya Yasir Isse** (sumeyayasir): Implemented the build processor + unit tests. Implemented the persistence layer and web interface for build history + unit tests.

- **Yiqin Lin** (Potoqin): Implemented Discord notifications(P8). Ensured cross-platform compatibility of server in test(Windows/Mac).

- **Emma Lindblom** (emmalindblm): Implemented CI result object + unit tests. Implemented git hook for issue reference consistency. Coordinated group and kept track of grading criteria.

- **Andy Li** (ydnall): Worked on webhook payload parsing, build pipeline integration, and commit status notification. Helped with documentation.

- **Martin Zivojinovic** (ZivoMartin): Introduced the initial Maven project structure. Configured Java 21 and the main entry point in pom.xml. Implemented the minimal webhook server listening on /webhook. Established the foundation of the CI server. Enabled jar packaging and runtime execution.

## License

See [LICENSE](LICENSE).

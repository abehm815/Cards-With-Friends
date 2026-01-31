# 1_srijita_5
# Cards With Friends

## Project Overview
Cards With Friends is a multiplayer Android card‑game app created as a four‑person project for **COMS 3090 at Iowa State University**. It combines a multi‑game hub (Blackjack, Euchre, Go Fish, and Crazy 8s) with a lobby system, real‑time chat, and competitive stats/leaderboards. The app is designed for quick matches with friends and persistent player stats across sessions.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HomeActivity.java†L13-L67】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyActivity.java†L1-L126】

### Credits
**Frontend (Android)**
- Alex Behm
- Eli Hearing

## Getting started
**Backend**
- Jake Bryfogel
- Colten Stevens

To make it easy for you to get started with GitLab, here's a list of recommended next steps.
---

Already a pro? Just edit this README.md and make it your own. Want to make it easy? [Use the template at the bottom](#editing-this-readme)!
## High‑Level App Flow (What a Player Experiences)
1. **Launch the app** → background music starts immediately for atmosphere.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/MainActivity.java†L17-L47】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/services/MusicService.java†L12-L41】
2. **Login or Sign Up** → create an account or authenticate an existing one.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/MainActivity.java†L17-L47】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LoginActivity.java†L16-L100】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/SignupActivity.java†L20-L114】
3. **Choose a game** from the home screen (Blackjack, Euchre, Go Fish, Crazy 8s).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HomeActivity.java†L18-L67】
4. **Create, join, or find a lobby** for that game (with live lobby listings).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyActivity.java†L23-L126】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/JoinActivity.java†L23-L166】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/FindLobbyActivity.java†L33-L219】
5. **In the lobby**, chat with players, see who is present, and start the game when ready (host only).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L31-L236】
6. **Play the game** using real‑time WebSocket updates and game‑specific controls (details below).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L34-L326】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/EuchreActivity.java†L76-L1187】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/GofishActivity.java†L29-L183】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L16-L141】
7. **Review stats, leaderboards, and match history** via the bottom navigation bar.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/services/BottomNavHelper.java†L13-L89】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/StatsActivity.java†L24-L204】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LeaderboardActivity.java†L25-L268】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HistoryActivity.java†L23-L187】

## Add your files
---

- [ ] [Create](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#create-a-file) or [upload](https://docs.gitlab.com/ee/user/project/repository/web_editor.html#upload-a-file) files
- [ ] [Add files using the command line](https://docs.gitlab.com/topics/git/add_files/#add-files-to-a-git-repository) or push an existing Git repository with the following command:
## Core Features (App‑Wide)

```
cd existing_repo
git remote add origin https://git.las.iastate.edu/cs309/2025fall/1_srijita_5.git
git branch -M main
git push -uf origin main
```
### 1) Account & Profile Management
- **Sign up** with first name, last name, email, age, username, and password.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/SignupActivity.java†L20-L114】
- **Login** with username/password (including dev/admin shortcuts).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LoginActivity.java†L16-L100】
- **Update profile** details from the profile screen.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ProfileActivity.java†L25-L72】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/UpdateActivity.java†L28-L125】
- **Delete profile stats** and return to the main screen (for resets).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ProfileActivity.java†L73-L113】
- **Logout** to return to the login screen.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ProfileActivity.java†L74-L83】

## Integrate with your tools
### 2) Home Hub + Persistent Navigation
- **Home hub** shows the four game tiles and routes you into lobby selection for that game.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HomeActivity.java†L18-L67】
- **Bottom navigation** provides quick access to Home, Stats, Leaderboard, and Profile across the app.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/services/BottomNavHelper.java†L13-L89】

- [ ] [Set up project integrations](https://git.las.iastate.edu/cs309/2025fall/1_srijita_5/-/settings/integrations)
### 3) Lobby System (Multiplayer Entry)
- **Host a lobby** (auto‑generated join code) or **join** with a code.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyActivity.java†L23-L126】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/JoinActivity.java†L23-L166】
- **Find active lobbies** in a live list (WebSocket‑driven).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/FindLobbyActivity.java†L33-L219】
- **Lobby view** displays player list, lobby code, and host controls.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L31-L236】
- **Kick detection** auto‑removes players who are kicked or whose lobby closes, with alert dialogs to return them to lobby selection.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L67-L210】

## Collaborate with your team
### 4) Lobby Chat (Real‑Time)
- **In‑lobby chat** via a full‑screen bottom sheet and WebSockets.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L465-L532】
- **Unread message counter** when chat is closed.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L37-L56】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L386-L394】

- [ ] [Invite team members and collaborators](https://docs.gitlab.com/ee/user/project/members/)
- [ ] [Create a new merge request](https://docs.gitlab.com/ee/user/project/merge_requests/creating_merge_requests.html)
- [ ] [Automatically close issues from merge requests](https://docs.gitlab.com/ee/user/project/issues/managing_issues.html#closing-issues-automatically)
- [ ] [Enable merge request approvals](https://docs.gitlab.com/ee/user/project/merge_requests/approvals/)
- [ ] [Set auto-merge](https://docs.gitlab.com/user/project/merge_requests/auto_merge/)
### 5) Stats & Competitive Tracking
- **Per‑game stats** (Blackjack, Euchre, Go Fish), shown in animated cards and switchable tabs.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/StatsActivity.java†L24-L204】
- **Leaderboards** ranked by win percentage per game with styled rank badges.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LeaderboardActivity.java†L25-L268】
- **Match history** with per‑match events and a drill‑down detail screen.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HistoryActivity.java†L23-L187】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/MatchDetailActivity.java†L8-L34】

## Test and Deploy
### 6) In‑App Audio
- **Background music** starts with the app and loops during play.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/MainActivity.java†L17-L47】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/services/MusicService.java†L12-L41】

Use the built-in continuous integration in GitLab.
---

- [ ] [Get started with GitLab CI/CD](https://docs.gitlab.com/ee/ci/quick_start/)
- [ ] [Analyze your code for known vulnerabilities with Static Application Security Testing (SAST)](https://docs.gitlab.com/ee/user/application_security/sast/)
- [ ] [Deploy to Kubernetes, Amazon EC2, or Amazon ECS using Auto Deploy](https://docs.gitlab.com/ee/topics/autodevops/requirements.html)
- [ ] [Use pull-based deployments for improved Kubernetes management](https://docs.gitlab.com/ee/user/clusters/agent/)
- [ ] [Set up protected environments](https://docs.gitlab.com/ee/ci/environments/protected_environments.html)
## Game Features (Per Game)

***
### Blackjack
- **Real‑time multiplayer gameplay** using WebSockets (lobby start → game state updates).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L34-L226】
- **Betting system** with chip balance display and bet validation before actions.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L87-L113】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BetHandler.java†L15-L152】
- **Dealer hand rendering** and animated card dealing/cleanup between rounds.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L287-L326】
- **Split support** (visibility toggles based on game state).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L362-L374】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackModels.java†L156-L176】

# Editing this README
### Euchre
- **Full gameplay loop** with bidding, trump selection, kitty card display, and trick tracking.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/EuchreActivity.java†L129-L1404】
- **Team score display** and **game‑over** view with winner information.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/EuchreActivity.java†L1100-L1168】
- **Host controls** (start game visibility for host only).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/EuchreActivity.java†L167-L176】

When you're ready to make this README your own, just edit this file and use the handy template below (or feel free to structure it however you want - this is just a starting point!). Thanks to [makeareadme.com](https://www.makeareadme.com/) for this template.
### Go Fish
- **Host‑driven game start**, real‑time player list, and a dynamic in‑game status display.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/GofishActivity.java†L50-L179】
- **Ask action** and server‑driven state updates over WebSockets.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/GofishActivity.java†L96-L183】

## Suggestions for a good README
### Crazy 8s
- **Live game status** (current player, current color, deck size).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L24-L136】
- **Draw penalties** and **color‑choice overlay** for Crazy 8s mechanics.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L49-L137】
- **Host auto‑start** sequence for new rounds when hosting.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L88-L141】

Every project is different, so consider which of these sections apply to yours. The sections used in the template are suggestions for most open source projects. Also keep in mind that while a README can be too long and detailed, too long is better than too short. If you think your README is too long, consider utilizing another form of documentation rather than cutting out information.
---

## Name
Choose a self-explaining name for your project.
## Admin & Moderator Tools
These are special flows accessible via hardcoded credentials in the login screen.

## Description
Let people know what your project can do specifically. Provide context and add a link to any reference visitors might be unfamiliar with. A list of Features or a Background subsection can also be added here. If there are alternatives to your project, this is a good place to list differentiating factors.
- **Admin dashboard**
  - Manage player chips (add/remove).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/AdminActivity.java†L42-L151】
  - Clean up empty lobbies and view current lobby lists.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/AdminActivity.java†L168-L280】
  - Remove players from lobbies, delete accounts, or adjust user stats.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/AdminActivity.java†L345-L565】
- **Moderator tools**
  - Create a stats record for a user by username (utility flow).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ModeratorActivity.java†L19-L92】

## Badges
On some READMEs, you may see small images that convey metadata, such as whether or not all the tests are passing for the project. You can use Shields to add some to your README. Many services also have instructions for adding a badge.
---

## Visuals
Depending on what you are making, it can be a good idea to include screenshots or even a video (you'll frequently see GIFs rather than actual videos). Tools like ttygif can help, but check out Asciinema for a more sophisticated method.
## Offline / Local Testing Mode
A temporary offline path exists that skips server authentication and launches the app as a special "offline" user. This is primarily for local testing or demos when the backend is unavailable.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LoginActivity.java†L41-L71】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HomeActivity.java†L35-L67】

## Installation
Within a particular ecosystem, there may be a common way of installing things, such as using Yarn, NuGet, or Homebrew. However, consider the possibility that whoever is reading your README is a novice and would like more guidance. Listing specific steps helps remove ambiguity and gets people to using your project as quickly as possible. If it only runs in a specific context like a particular programming language version or operating system or has dependencies that have to be installed manually, also add a Requirements subsection.
---

## Usage
Use examples liberally, and show the expected output if you can. It's helpful to have inline the smallest example of usage that you can demonstrate, while providing links to more sophisticated examples if they are too long to reasonably include in the README.
## Backend Integration Summary (What the App Talks To)
- **REST API** endpoints power user accounts, lobbies, stats, leaderboards, and match history (hosted on `coms-3090-006.class.las.iastate.edu:8080`).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LoginActivity.java†L30-L83】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/StatsActivity.java†L24-L95】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HistoryActivity.java†L33-L88】
- **WebSocket services** drive real‑time lobby updates, in‑lobby chat, and live gameplay for each game type.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/FindLobbyActivity.java†L74-L121】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L210-L358】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/GofishActivity.java†L109-L170】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L88-L141】

## Support
Tell people where they can go to for help. It can be any combination of an issue tracker, a chat room, an email address, etc.
---

## Roadmap
If you have ideas for releases in the future, it is a good idea to list them in the README.
## Full Feature Checklist (Quick Reference)
**Authentication & Profiles**
- Sign up, login, update profile, delete stats, logout.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LoginActivity.java†L16-L100】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/SignupActivity.java†L20-L114】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ProfileActivity.java†L25-L113】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/UpdateActivity.java†L28-L125】

## Contributing
State if you are open to contributions and what your requirements are for accepting them.
**Home & Navigation**
- Game selection hub and persistent bottom nav for quick switching.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HomeActivity.java†L18-L67】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/services/BottomNavHelper.java†L13-L89】

For people who want to make changes to your project, it's helpful to have some documentation on how to get started. Perhaps there is a script that they should run or some environment variables that they need to set. Make these steps explicit. These instructions could also be useful to your future self.
**Lobbies**
- Host lobby, join by code, live lobby discovery, lobby management (leave/delete).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyActivity.java†L23-L126】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/JoinActivity.java†L23-L166】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/FindLobbyActivity.java†L33-L219】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L398-L459】

You can also document commands to lint the code or run tests. These steps help to ensure high code quality and reduce the likelihood that the changes inadvertently break something. Having instructions for running tests is especially helpful if it requires external setup, such as starting a Selenium server for testing in a browser.
**Lobby Chat & Presence**
- Player list, unread message tracking, full‑screen chat sheet, real‑time joins/leaves.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LobbyViewActivity.java†L256-L532】

## Authors and acknowledgment
Show your appreciation to those who have contributed to the project.
**Games**
- Blackjack (betting, dealer UI, split, live round updates).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/blackjack/BlackjackActivity.java†L34-L374】
- Euchre (bidding, trump/kitty, scoring, game‑over flow).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/EuchreActivity.java†L129-L1168】
- Go Fish (ask action, host start, live state).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/GofishActivity.java†L50-L183】
- Crazy 8s (color selection, draw penalties, deck status).【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/crazy8/Crazy8Activity.java†L24-L141】

## License
For open source projects, say how it is licensed.
**Stats / Leaderboards / History**
- Per‑game stat cards, win‑percentage leaderboards, and match event history with detail view.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/StatsActivity.java†L24-L204】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/LeaderboardActivity.java†L25-L268】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/HistoryActivity.java†L23-L187】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/MatchDetailActivity.java†L8-L34】

## Project status
If you have run out of energy or time for your project, put a note at the top of the README saying that development has slowed down or stopped completely. Someone may choose to fork your project or volunteer to step in as a maintainer or owner, allowing your project to keep going. You can also make an explicit request for maintainers.
**Admin & Moderator**
- Admin lobby cleanup, player management, chip edits; moderator stats creation flow.【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/AdminActivity.java†L30-L565】【F:Frontend/CardsWithFriends/app/src/main/java/com/example/androidexample/ModeratorActivity.java†L19-L92】

---

## Course Context
This project was completed as part of **COMS 3090** at **Iowa State University** in a four‑person team setting. It was scoped as a complete, full‑stack multiplayer Android experience, emphasizing feature breadth (multiple games, live lobbies, and player stats) over production deployment.

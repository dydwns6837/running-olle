# Running Olle

러닝과 여행을 연결하는 위치 기반 서비스입니다.

## 프로젝트 구조

```text
running-olle/
├── frontend/
├── backend/
├── docs/
├── infra/
├── .github/
├── README.md
└── .gitignore
```

## Frontend

- React
- Vite
- TypeScript
- React Router
- Axios
- Zustand
- Tailwind CSS
- vite-plugin-pwa

```bash
cd frontend
npm install
npm run dev
```

## Backend

- Spring Boot 3.x
- Java 17
- Gradle
- Spring Data JPA
- PostgreSQL
- PostGIS

```bash
cd backend
gradle bootRun
```

로컬 DB 설정은 `application-secret.yml`로 만든 뒤 실제 값을 입력합니다.

## Branch Strategy

- `main`: 최종 안정 버전
- `develop`: 개발 통합 브랜치
- `feature/*`: 기능 개발 브랜치

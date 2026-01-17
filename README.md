# CallFlow Visualizer

An IntelliJ IDEA plugin that visualizes method call flows as interactive graphs. Analyze caller/callee relationships with a single right-click.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![IntelliJ IDEA](https://img.shields.io/badge/IntelliJ%20IDEA-2024.1+-blue.svg)](https://www.jetbrains.com/idea/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple.svg)](https://kotlinlang.org/)

## Purpose

Understanding complex codebases can be challenging, especially when tracking method call relationships across multiple classes. **CallFlow Visualizer** helps developers:

- **Visualize call hierarchies** - See who calls a method (callers) and what it calls (callees)
- **Navigate code quickly** - Single-click on any node to jump to source code
- **Understand dependencies** - Identify tightly coupled components at a glance
- **Debug faster** - Trace execution paths visually instead of mentally

## Features

### Graph View
- **Left-to-right layout** for intuitive call flow visualization
- **Interactive nodes** with single-click navigation to source code
- **Zoom & Pan** - Mouse wheel to zoom, drag to pan
- **Color-coded badges** for different node types:
  - `Service` - Spring @Service classes (green)
  - `Repository` - Data access layer (cyan)
  - `Controller` - REST endpoints (blue)
  - `Interface` - Interface types (blue-grey)
  - `Entity` - JPA @Entity classes (orange)
  - `Class` - Regular classes (grey)

### Tree View
- Hierarchical tree representation of call relationships
- Expandable/collapsible nodes
- Quick navigation support

### Analysis Options
- **Direction**: Callers Only, Callees Only, or Both
- **Depth**: Configure analysis depth (1-10 levels)
- **Hide Entities**: Toggle to show/hide Entity class nodes
- **Start Node Highlight**: Red border with star marker for the analyzed method

### Export
- **Mermaid** diagram format
- **PlantUML** diagram format
- Copy to clipboard with one click

## Installation

### From JetBrains Marketplace (Recommended)
1. Open IntelliJ IDEA
2. Go to `Settings/Preferences` > `Plugins` > `Marketplace`
3. Search for "CallFlow Visualizer"
4. Click `Install`

### Manual Installation
1. Download the latest release from [Releases](https://github.com/jkshin0602/CallFlow-Visualizer/releases)
2. Go to `Settings/Preferences` > `Plugins` > `Settings icon` > `Install Plugin from Disk`
3. Select the downloaded `.zip` file

## Usage

### Basic Usage
1. Open any Java or Kotlin file
2. Right-click on a method name
3. Select **"Analyze Call Flow"** from the context menu
4. View the result in the CallFlow tool window

### Direction-Specific Analysis
- **Analyze Callers**: Right-click > "Analyze Callers" (shows only upstream calls)
- **Analyze Callees**: Right-click > "Analyze Callees" (shows only downstream calls)

### Navigation
- **Single-click** any node to navigate to its source code
- **Mouse wheel** to zoom in/out
- **Drag** to pan the view
- **Fit** button to fit all nodes in view
- **100%** button to reset zoom

### Toolbar Options
| Option | Description |
|--------|-------------|
| Direction | Choose between Both, Callers Only, or Callees Only |
| Depth | Set analysis depth (1-10 levels) |
| Hide Entities | Toggle visibility of Entity class nodes |
| Refresh | Re-run analysis with current settings |
| Export | Export to Mermaid or PlantUML format |

## Requirements

- IntelliJ IDEA 2024.1 or later
- Java 17 or later (for plugin runtime)
- Java or Kotlin project

## Building from Source

```bash
# Clone the repository
git clone https://github.com/jkshin0602/CallFlow-Visualizer.git
cd CallFlow-Visualizer

# Build the plugin
./gradlew buildPlugin

# The plugin zip will be in build/distributions/
```

## Feedback & Support

For bug reports, feature requests, or questions:
- Email: base6666@naver.com
- GitHub Issues: [Create an issue](https://github.com/jkshin0602/CallFlow-Visualizer/issues)

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

# CallFlow Visualizer (한국어)

메서드 호출 흐름을 인터랙티브 그래프로 시각화하는 IntelliJ IDEA 플러그인입니다. 우클릭 한 번으로 caller/callee 관계를 분석할 수 있습니다.

## 목적

복잡한 코드베이스에서 메서드 호출 관계를 추적하는 것은 어려운 일입니다. **CallFlow Visualizer**는 개발자가 다음을 할 수 있도록 도와줍니다:

- **호출 계층 시각화** - 메서드를 호출하는 곳(callers)과 호출되는 곳(callees)을 한눈에 파악
- **빠른 코드 네비게이션** - 노드 클릭으로 소스 코드로 즉시 이동
- **의존성 파악** - 밀접하게 결합된 컴포넌트를 시각적으로 확인
- **빠른 디버깅** - 실행 경로를 머릿속이 아닌 시각적으로 추적

## 주요 기능

### 그래프 뷰
- **좌에서 우로** 직관적인 호출 흐름 레이아웃
- **인터랙티브 노드** - 싱글 클릭으로 소스 코드 이동
- **줌 & 팬** - 마우스 휠로 확대/축소, 드래그로 이동
- **색상 코드 배지**:
  - `Service` - Spring @Service 클래스 (초록색)
  - `Repository` - 데이터 접근 계층 (청록색)
  - `Controller` - REST 엔드포인트 (파란색)
  - `Interface` - 인터페이스 타입 (회청색)
  - `Entity` - JPA @Entity 클래스 (주황색)
  - `Class` - 일반 클래스 (회색)

### 트리 뷰
- 호출 관계의 계층적 트리 표현
- 펼치기/접기 가능한 노드
- 빠른 네비게이션 지원

### 분석 옵션
- **방향**: Callers Only, Callees Only, 또는 Both (양방향)
- **깊이**: 분석 깊이 설정 (1-10 레벨)
- **Entity 숨기기**: Entity 클래스 노드 표시/숨김 토글
- **시작 노드 하이라이트**: 분석 대상 메서드에 빨간색 테두리와 별 마커 표시

### 내보내기
- **Mermaid** 다이어그램 형식
- **PlantUML** 다이어그램 형식
- 클릭 한 번으로 클립보드에 복사

## 설치 방법

### JetBrains Marketplace에서 설치 (권장)
1. IntelliJ IDEA 실행
2. `Settings/Preferences` > `Plugins` > `Marketplace` 이동
3. "CallFlow Visualizer" 검색
4. `Install` 클릭

### 수동 설치
1. [Releases](https://github.com/jkshin0602/CallFlow-Visualizer/releases)에서 최신 버전 다운로드
2. `Settings/Preferences` > `Plugins` > `설정 아이콘` > `Install Plugin from Disk` 선택
3. 다운로드한 `.zip` 파일 선택

## 사용 방법

### 기본 사용법
1. Java 또는 Kotlin 파일 열기
2. 메서드 이름에서 우클릭
3. 컨텍스트 메뉴에서 **"Analyze Call Flow"** 선택
4. CallFlow 도구 창에서 결과 확인

### 방향별 분석
- **Analyze Callers**: 우클릭 > "Analyze Callers" (호출하는 쪽만 표시)
- **Analyze Callees**: 우클릭 > "Analyze Callees" (호출되는 쪽만 표시)

### 네비게이션
- **싱글 클릭**: 노드의 소스 코드로 이동
- **마우스 휠**: 확대/축소
- **드래그**: 뷰 이동
- **Fit 버튼**: 모든 노드를 뷰에 맞춤
- **100% 버튼**: 줌 리셋

### 툴바 옵션
| 옵션 | 설명 |
|------|------|
| Direction | Both, Callers Only, Callees Only 중 선택 |
| Depth | 분석 깊이 설정 (1-10 레벨) |
| Hide Entities | Entity 클래스 노드 표시/숨김 |
| Refresh | 현재 설정으로 분석 재실행 |
| Export | Mermaid 또는 PlantUML 형식으로 내보내기 |

## 요구 사항

- IntelliJ IDEA 2024.1 이상
- Java 17 이상 (플러그인 런타임용)
- Java 또는 Kotlin 프로젝트

## 소스에서 빌드

```bash
# 저장소 클론
git clone https://github.com/jkshin0602/CallFlow-Visualizer.git
cd CallFlow-Visualizer

# 플러그인 빌드
./gradlew buildPlugin

# 플러그인 zip 파일은 build/distributions/에 생성됨
```

## 피드백 및 지원

버그 리포트, 기능 요청, 또는 질문:
- 이메일: base6666@naver.com
- GitHub Issues: [이슈 생성](https://github.com/jkshin0602/CallFlow-Visualizer/issues)

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다 - 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

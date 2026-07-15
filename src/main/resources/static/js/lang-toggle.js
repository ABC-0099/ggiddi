/**
 * 끼역띠귿 - 메인/가입 페이지 KO/EN 토글
 * -------------------------------------------------
 * 사용법:
 * 1. 이 파일을 /static/js/lang-toggle.js 에 저장
 * 2. 각 페이지 </body> 직전에 추가:
 *      <script src="/js/lang-toggle.js"></script>
 * 3. 번역이 필요한 요소에 data-i18n="key" 속성 부여
 *      예) <h1 data-i18n="hero.badge">한국어 학습 플랫폼</h1>
 *      예) <input placeholder="이메일" data-i18n-placeholder="signup.field.email_ph">
 * 4. 아래 DICTIONARY 에 key 별 en 텍스트 채우기 (ko는 기존 마크업 그대로 사용)
 * 5. 토글 버튼을 원하는 위치에 삽입:
 *      <button id="langToggleBtn" class="lang-toggle-btn">EN</button>
 *
 * 동작 방식:
 * - localStorage 에 'kkyeok_lang' 저장 (ko / en)
 * - 페이지 로드 시 저장된 언어 자동 적용
 * - data-i18n 요소는 textContent를, data-i18n-placeholder는 placeholder를 교체
 * - 원본 한국어는 data-i18n-ko 로 자동 백업되어 EN -> KO 전환 시 복원됨
 */

(function () {
  const STORAGE_KEY = "kkyeok_lang";

  // -----------------------------
  // 번역 딕셔너리 (필요한 key 계속 추가하면 됨)
  // -----------------------------
  const DICTIONARY = {
    en: {
      // navbar.html
      "nav.notice": "Notice",
      "nav.community": "Community",
      "nav.event": "Event",
      "nav.lectures": "Learn",
      "nav.practice": "Practice",
      "nav.qna_center": "Help Center",
      "nav.lectures.video": "Video Lectures",
      "nav.lectures.video_desc": "Step-by-step Korean lessons",
      "nav.lectures.game": "Learning Games",
      "nav.lectures.game_desc": "Quizzes & action games",
      "nav.practice.quiz": "Practice Quiz",
      "nav.practice.quiz_desc": "Light practice by unit",
      "nav.practice.mock": "Mock Exam",
      "nav.practice.mock_desc": "Timed real-test practice",
      "nav.practice.wrong": "Wrong Answer Notes",
      "nav.practice.wrong_desc": "Review your mistakes",
      "nav.qna.faq": "FAQ",
      "nav.qna.ask": "Ask a Question",
      "nav.qna.ask_desc": "Ask instructors directly",
      "nav.login": "Log In",
      "nav.signup": "Sign Up",
      "nav.logout": "Log Out",

      // footer.html
      "footer.tagline.line1": "A Korean learning platform",
      "footer.tagline.line2": "made for learners abroad",
      "footer.col.learn": "Learn",
      "footer.learn.lecture": "Video Lectures",
      "footer.learn.game": "Learning Games",
      "footer.learn.pricing": "Subscription Plans",
      "footer.col.community": "Community",
      "footer.col.support": "Support",
      "footer.support.terms": "Terms of Service",
      "footer.support.privacy": "Privacy Policy",
      "footer.support.mypage": "My Page",
      "footer.support.admin": "Admin Dashboard",

      // mainpage.html - hero
      "hero.badge": "Korean Learning Platform",
      "hero.title.1": "Korean,",
      "hero.title.2": "learned together,",
      "hero.title.3": "is easier",
      "hero.sub.1": "A game-based Korean learning platform",
      "hero.sub.2": "made for learners from abroad.",
      "hero.cta.start": "Start for Free",
      "hero.cta.curriculum": "View Curriculum",

      // mainpage.html - login card (anonymous hero)
      "login.title": "Log In",
      "login.subtitle": "Log in to start learning",
      "login.id": "Username",
      "login.id_ph": "Enter your username",
      "login.pw": "Password",
      "login.pw_ph": "Enter your password",
      "login.submit": "Log In",
      "login.or": "or",
      "login.go_signup": "Sign Up",
      "login.find_id": "Find Username",
      "login.find_pw": "Find Password",

      // mainpage.html - dashboard (logged-in hero)
      "dash.admin.new_users": "New Signups Today",
      "dash.admin.reports": "Pending Reports",
      "dash.admin.qna": "Pending Questions",
      "dash.admin.goto": "Go to Admin Page →",
      "dash.user.greeting": "Ready for today's Korean adventure!",
      "dash.user.completed": "Lessons Completed",
      "dash.user.streak": "Day Streak",
      "dash.user.progress": "Overall Progress",
      "dash.user.continue": "Continue Learning →",

      // mainpage.html - stats strip
      "stats.learners": "Total Learners",
      "stats.rating": "Average Rating",
      "stats.curriculum": "Structured Stages",

      // mainpage.html - why section
      "why.tag": "Why Kkiyeok-Tigeut",
      "why.title.1": "Learning that bores you into quitting —",
      "why.title.2": "it's time for something different",
      "why.problem.label": "The Problem",
      "why.problem.title": "Learners burn out on repetition",
      "why.problem.desc": "Demand for learning Korean is growing worldwide, but most services still rely on tedious repetition, failing to keep learners interested and coming back.",
      "why.solution.label": "Our Solution",
      "why.solution.title": "Korean learning that feels like a game",
      "why.solution.desc": "That's why Kkiyeok-Tigeut combines word games, action games, live quizzes, and community into one experience learners actually want to keep coming back to.",

      // mainpage.html - curriculum section
      "curriculum.tag": "Curriculum",
      "curriculum.title.1": "From K-POP to advanced Korean,",
      "curriculum.title.2": "completed the fun way",
      "curriculum.desc": "Build interest with K-POP and K-Dramas, move through everyday conversation, and naturally work up to advanced expressions.",
      "curriculum.theme1.badge": "Theme 1",
      "curriculum.theme1.title": "K-POP",
      "curriculum.theme2.badge": "Theme 2",
      "curriculum.theme2.title": "Iconic Drama Lines",
      "curriculum.theme3.badge": "Theme 3",
      "curriculum.theme3.title": "Everyday Conversation",
      "curriculum.theme4.badge": "Theme 4",
      "curriculum.theme4.title": "Advanced",

      // mainpage.html - features section
      "features.tag": "How You'll Learn",
      "features.title.1": "Learning has to be fun",
      "features.title.2": "to actually stick",
      "features.desc": "Games, quizzes, and community come together in one immersive learning experience.",
      "features.word.title": "Word Game",
      "features.word.desc": "Guess words against the clock to build vocabulary. Fast, game-like repetition helps words stick without even trying.",
      "features.action.title": "Action Game",
      "features.action.desc": "Control a character to recover missing consonants. Learning through movement makes it stay in memory longer.",
      "features.quiz.title": "Step-by-Step Quizzes",
      "features.quiz.desc": "Check your understanding with a quiz after each stage. Review what you got wrong until it truly sticks.",
      "features.community.title": "Learner Community",
      "features.community.desc": "Connect with learners in situations like yours and share experiences. You're never learning alone.",

      // mainpage.html - reviews section
      "reviews.tag": "Learner Reviews",
      "reviews.title.1": "Hear from people",
      "reviews.title.2": "who've actually learned here",
      "reviews.desc": "Honest reviews from learners who studied Korean with Kkiyeok-Tigeut.",

      // mainpage.html - CTA section
      "cta.title.1": "Start your ",
      "cta.title.2": "Korean journey",
      "cta.title.3": "",
      "cta.title.4": "today",
      "cta.subtitle": "Try the first stage for free.",
      "cta.start": "Start for Free Now",
      "cta.curriculum": "Browse the Curriculum",

      // chuga.html - left panel
      "signup.tag": "Korean Learning Platform",
      "signup.h1.1": "Sign up now and",
      "signup.h1.2": "start your adventure",
      "signup.sub.1": "From K-POP to advanced Korean with Kkiyeok-Tigeut,",
      "signup.sub.2": "learn Korean the fun way 🎮",
      "signup.benefit1.title": "Learn with Quiz & Action Games",
      "signup.benefit1.desc": "Pick up Korean naturally while solving quizzes and playing action games",
      "signup.benefit2.title": "Track Your Own Progress",
      "signup.benefit2.desc": "See your stage progress and quiz results at a glance",
      "signup.benefit3.title": "Join the Learner Community",
      "signup.benefit3.desc": "Learn together with learners who share your goals",
      "signup.benefit4.title": "1 Free Lesson",
      "signup.benefit4.desc": "Try one lesson from any theme for free right after signing up",

      // chuga.html - signup card header
      "signup.card_title": "Sign Up",
      "signup.card_sub": "A simple signup and you're ready to start",
      "signup.kakao": "Kakao",
      "signup.divider": "or sign up with email",
      "signup.step1": "Basic Info",
      "signup.step2": "Account Setup",
      "signup.step3": "Agreements",

      // chuga.html - form fields
      "signup.required": "Required",
      "signup.optional": "Optional",
      "signup.field.name": "Name",
      "signup.field.name_ph": "e.g. John Doe",
      "signup.field.userid": "Username",
      "signup.field.userid_ph": "4-20 lowercase letters and numbers",
      "signup.check_dup": "Check Availability",
      "signup.field.userid_hint": "Use 4-20 lowercase letters and numbers.",
      "signup.field.birth": "Date of Birth",
      "signup.field.phone": "Phone Number",
      "signup.request_verify": "Request Code",
      "signup.confirm_verify": "I got the text, confirm",
      "signup.field.email": "Email",
      "signup.field.pw": "Password",
      "signup.field.pw_ph": "8+ characters, letters + numbers",
      "signup.field.pw2": "Confirm Password",
      "signup.field.pw2_ph": "Re-enter your password",
      "signup.field.nation": "Nationality",
      "signup.field.nation_hint": "Used to personalize your learning content.",
      "signup.nation.placeholder": "Select nationality (optional)",
      "signup.nation.cn": "🇨🇳 China",
      "signup.nation.vn": "🇻🇳 Vietnam",
      "signup.nation.ph": "🇵🇭 Philippines",
      "signup.nation.jp": "🇯🇵 Japan",
      "signup.nation.us": "🇺🇸 USA",
      "signup.nation.mn": "🇲🇳 Mongolia",
      "signup.nation.th": "🇹🇭 Thailand",
      "signup.nation.id": "🇮🇩 Indonesia",
      "signup.nation.eu": "🇪🇺 Europe",
      "signup.nation.af": "🌍 Africa",
      "signup.nation.etc": "Other",

      // chuga.html - agreements & submit
      "signup.agree_all": "Agree to All",
      "signup.agree.terms": "Agree to Terms of Service",
      "signup.agree.privacy": "Agree to Privacy Policy",
      "signup.agree.marketing": "Agree to receive marketing info",
      "signup.agree.view": "View",
      "signup.submit": "Complete Sign Up",
      "signup.has_account": "Already have an account?",
      "signup.go_login": "Log In",
      "signup.terms.1": "By signing up, you agree to Kkiyeok-Tigeut's",
      "signup.terms.tos": "Terms of Service",
      "signup.terms.2": "and",
      "signup.terms.privacy": "Privacy Policy",
      "signup.terms.3": ".",

      // login.html
      "login.tag": "Korean Learning Platform",
      "login.h1.1": "Welcome",
      "login.h1.2": "back",
      "login.sub.1": "Pick up right where you left off with Kkiyeok-Tigeut —",
      "login.sub.2": "your Korean adventure continues today!",
      "login.card_title": "Log In",
      "login.card_sub": "Log in to your account and keep learning",
      "login.err_invalid": "Incorrect username or password.",
      "login.msg_logout": "You've been logged out.",
      "login.msg_duplicated": "You've been logged out automatically because you logged in elsewhere.",
      "login.id_ph2": "Enter your username",
      "login.pw_ph2": "Enter your password",
      "login.remember": "Keep me logged in",
      "login.find_link": "Find Username / Password",
      "login.social_divider": "or log in with a social account",
      "login.no_account": "Don't have an account yet?",
      "login.go_signup2": "Sign Up",
      "login.terms.1": "By logging in, you agree to Kkiyeok-Tigeut's",

      // drag-translate.html (번역 위젯 본체 - 온보딩 팝업은 항상 영어 고정이라 토글 대상 아님)
      "dt.trigger_btn": "Translate",
      "dt.popup_title": "Translation",
      "dt.loading": "Translating...",
    },
  };

  function getLang() {
    return localStorage.getItem(STORAGE_KEY) || "ko";
  }

  function setLang(lang) {
    localStorage.setItem(STORAGE_KEY, lang);
    applyLang(lang);
    updateButton(lang);
    // 언어가 바뀔 때마다 다른 스크립트(예: 드래그 번역 온보딩)가 반응할 수 있게 이벤트 발행
    document.dispatchEvent(new CustomEvent("kkyeoklangchange", { detail: { lang } }));
  }

  function applyLang(lang) {
    // 텍스트 콘텐츠 교체
    document.querySelectorAll("[data-i18n]").forEach((el) => {
      const key = el.getAttribute("data-i18n");

      // 원본 한국어 백업 (최초 1회)
      if (!el.hasAttribute("data-i18n-ko")) {
        el.setAttribute("data-i18n-ko", el.textContent);
      }

      if (lang === "ko") {
        el.textContent = el.getAttribute("data-i18n-ko");
      } else {
        const translated = DICTIONARY[lang] && DICTIONARY[lang][key];
        el.textContent = translated || el.getAttribute("data-i18n-ko");
      }
    });

    // placeholder 교체
    document.querySelectorAll("[data-i18n-placeholder]").forEach((el) => {
      const key = el.getAttribute("data-i18n-placeholder");

      if (!el.hasAttribute("data-i18n-placeholder-ko")) {
        el.setAttribute("data-i18n-placeholder-ko", el.getAttribute("placeholder") || "");
      }

      if (lang === "ko") {
        el.setAttribute("placeholder", el.getAttribute("data-i18n-placeholder-ko"));
      } else {
        const translated = DICTIONARY[lang] && DICTIONARY[lang][key];
        el.setAttribute("placeholder", translated || el.getAttribute("data-i18n-placeholder-ko"));
      }
    });

    document.documentElement.setAttribute("lang", lang === "ko" ? "ko" : "en");
  }

  function updateButton(lang) {
    const btn = document.getElementById("langToggleBtn");
    if (!btn) return;
    btn.textContent = lang === "ko" ? "EN" : "한국어";
    btn.setAttribute("aria-label", lang === "ko" ? "Switch to English" : "한국어로 보기");
  }

  function toggleLang() {
    const current = getLang();
    setLang(current === "ko" ? "en" : "ko");
  }

  document.addEventListener("DOMContentLoaded", function () {
    const lang = getLang();
    applyLang(lang);
    updateButton(lang);

    const btn = document.getElementById("langToggleBtn");
    if (btn) {
      btn.addEventListener("click", toggleLang);
    }
  });

  // 외부에서 필요하면 호출 가능하도록 노출
  window.KkyeokLang = { getLang, setLang, toggleLang };
})();
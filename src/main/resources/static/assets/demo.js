(() => {
  try {
    const canvas = document.getElementById("demoCanvas");
    if (!canvas) {
      console.error("[demo] #demoCanvas not found");
      return;
    }

    const scoreEl = document.getElementById("demoScore");
    const bestEl = document.getElementById("demoBest");
    const restartBtn = document.getElementById("demoRestart");

    const demoRoot = canvas.closest(".demo");

    const ctx = canvas.getContext("2d");
    const W = canvas.width, H = canvas.height;

    const cell = 24;
    const cols = Math.floor(W / cell);
    const rows = Math.floor(H / cell);


    canvas.setAttribute("tabindex", "0"); // make sure it can receive focus
    let demoActive = false;

    function setDemoActive(active) {
      demoActive = active;
      demoRoot?.classList.toggle("demo-active", active);
    }

    const SWIPE_THRESHOLD = 24;
    let swipe = null;

    function queueDir(x, y) {
      state.nextDir = {x, y};
    }

    function queueDirByName(name) {
      if (name === "up") queueDir(0, -1);
      else if (name === "down") queueDir(0, 1);
      else if (name === "left") queueDir(-1, 0);
      else if (name === "right") queueDir(1, 0);
    }

    canvas.addEventListener("pointerdown", (e) => {
      canvas.focus({ preventScroll: true });
      setDemoActive(true);

      const isTouch = e.pointerType === "touch" || e.pointerType === "pen";
      if (isTouch && !swipe) {
        swipe = {
          pointerId: e.pointerId,
          startX: e.clientX,
          startY: e.clientY,
          used: false,
        };

        try {
          canvas.setPointerCapture(e.pointerId);
        } catch (err) {
          // ignore
        }
      }
    }, { passive: false });

    canvas.addEventListener("pointermove", (e) => {
      if (!swipe || swipe.pointerId !== e.pointerId || swipe.used) return;

      const dx = e.clientX - swipe.startX;
      const dy = e.clientY - swipe.startY;
      const ax = Math.abs(dx);
      const ay = Math.abs(dy);

      if (Math.max(ax, ay) < SWIPE_THRESHOLD) return;

      if (ax > ay) queueDir(dx > 0 ? 1 : -1, 0);
      else queueDir(0, dy > 0 ? 1 : -1);

      swipe.used = true;
      e.preventDefault();
    }, { passive: false });

    function endSwipe(e) {
      if (swipe && swipe.pointerId === e.pointerId) swipe = null;
    }

    canvas.addEventListener("pointerup", endSwipe);
    canvas.addEventListener("pointercancel", endSwipe);

    canvas.addEventListener("focus", () => setDemoActive(true));
    canvas.addEventListener("blur", () => {
      requestAnimationFrame(() => {
        if (demoRoot && demoRoot.contains(document.activeElement)) return;
        setDemoActive(false);
      });
    });

    demoRoot?.addEventListener("pointerdown", (e) => {
      if (!(e.target instanceof Element)) return;

      const btn = e.target.closest("[data-demo-dir]");
      if (!btn || !demoRoot.contains(btn)) return;

      e.preventDefault();
      canvas.focus({ preventScroll: true });
      setDemoActive(true);

      const dir = btn.getAttribute("data-demo-dir");
      if (dir) queueDirByName(dir);
    }, { passive: false });

    // If user clicks anywhere outside the demo, deactivate demo controls
    document.addEventListener("pointerdown", (e) => {
      if (demoRoot) {
        if (!demoRoot.contains(e.target)) setDemoActive(false);
      } else if (e.target !== canvas) {
        setDemoActive(false);
      }
    });


    let best = 0;

    const state = {
      snake: [],
      dir: {x: 1, y: 0},
      nextDir: {x: 1, y: 0},
      food: {x: 10, y: 10},
      score: 0,
      alive: true,
      t: 0,
      speed: 8.5,
    };

    const rand = (n) => Math.floor(Math.random() * n);

    function updateUI() {
      if (scoreEl) scoreEl.textContent = String(state.score);
      if (bestEl) bestEl.textContent = String(best);
    }

    function placeFood() {
      let tries = 0;
      while (tries++ < 2000) {
        const f = {x: rand(cols), y: rand(rows)};
        if (!state.snake.some(s => s.x === f.x && s.y === f.y)) {
          state.food = f;
          return;
        }
      }
    }

    function reset() {
      state.snake = [{x: 6, y: 8}, {x: 5, y: 8}, {x: 4, y: 8}];
      state.dir = {x: 1, y: 0};
      state.nextDir = {x: 1, y: 0};
      state.score = 0;
      state.alive = true;
      state.t = 0;
      placeFood();
      updateUI();
      draw(); // draw immediately so it never looks blank
    }

    function cellRect(x, y, pad = 4) {
      const px = x * cell + pad;
      const py = y * cell + pad;
      return [px, py, cell - pad*2, cell - pad*2];
    }

    function roundRect(c, x, y, w, h, r) {
      const rr = Math.min(r, w/2, h/2);
      c.beginPath();
      c.moveTo(x + rr, y);
      c.arcTo(x + w, y, x + w, y + h, rr);
      c.arcTo(x + w, y + h, x, y + h, rr);
      c.arcTo(x, y + h, x, y, rr);
      c.arcTo(x, y, x + w, y, rr);
      c.closePath();
    }

    function draw() {
      ctx.clearRect(0,0,W,H);
      ctx.fillStyle = "rgba(0,0,0,.12)";
      ctx.fillRect(0,0,W,H);

      // grid
      ctx.strokeStyle = "rgba(255,255,255,.05)";
      for (let x = 0; x <= cols; x++) {
        ctx.beginPath();
        ctx.moveTo(x * cell + 0.5, 0);
        ctx.lineTo(x * cell + 0.5, H);
        ctx.stroke();
      }
      for (let y = 0; y <= rows; y++) {
        ctx.beginPath();
        ctx.moveTo(0, y * cell + 0.5);
        ctx.lineTo(W, y * cell + 0.5);
        ctx.stroke();
      }

      // food
      {
        const [x,y,w,h] = cellRect(state.food.x, state.food.y, 6);
        ctx.fillStyle = "rgba(255,211,110,.22)";
        ctx.beginPath();
        ctx.arc(x + w/2, y + h/2, w * 0.85, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = "rgba(255,211,110,.9)";
        ctx.beginPath();
        ctx.arc(x + w/2, y + h/2, w * 0.35, 0, Math.PI * 2);
        ctx.fill();
      }

      // snake
      for (let i = 0; i < state.snake.length; i++) {
        const s = state.snake[i];
        const [x,y,w,h] = cellRect(s.x, s.y, 4);

        const head = i === 0;
        ctx.fillStyle = head ? "rgba(120,166,255,.78)" : "rgba(64,242,201,.20)";
        ctx.strokeStyle = "rgba(255,255,255,.12)";

        roundRect(ctx, x, y, w, h, 10);
        ctx.fill();
        ctx.stroke();

        if (head) {
          ctx.fillStyle = "rgba(255,255,255,.86)";
          ctx.beginPath();
          ctx.arc(x + w*0.70, y + h*0.35, 2.6, 0, Math.PI*2);
          ctx.arc(x + w*0.70, y + h*0.65, 2.6, 0, Math.PI*2);
          ctx.fill();
        }
      }

      if (!state.alive) {
        ctx.fillStyle = "rgba(0,0,0,.45)";
        ctx.fillRect(0,0,W,H);
        ctx.fillStyle = "rgba(255,255,255,.92)";
        ctx.font = "700 28px ui-sans-serif, system-ui";
        ctx.fillText("Game Over", 28, 64);
        ctx.fillStyle = "rgba(255,255,255,.72)";
        ctx.font = "14px ui-sans-serif, system-ui";
        ctx.fillText("Press Restart (or Space) to play again.", 28, 92);
      }
    }

    function step() {
      if (!state.alive) return;

      // prevent reverse
      if (!(state.nextDir.x === -state.dir.x && state.nextDir.y === -state.dir.y)) {
        state.dir = state.nextDir;
      }

      const head = state.snake[0];
      const nx = (head.x + state.dir.x + cols) % cols;
      const ny = (head.y + state.dir.y + rows) % rows;

      const tail = state.snake[state.snake.length - 1];
      const hitSelf = state.snake.some(p => p.x === nx && p.y === ny) && !(tail.x === nx && tail.y === ny);

      if (hitSelf) {
        state.alive = false;
        best = Math.max(best, state.score);
        updateUI();
        return;
      }

      state.snake.unshift({x: nx, y: ny});

      const ate = (nx === state.food.x && ny === state.food.y);
      if (ate) {
        state.score += 10;
        best = Math.max(best, state.score);
        placeFood();
        updateUI();
      } else {
        state.snake.pop();
      }
    }

    function onKey(e) {
      if (!demoActive) return; // <-- allow normal page scroll outside demo

      const k = e.key.toLowerCase();

      // Prevent browser scrolling / page movement ONLY while demo is active
      if (k.startsWith("arrow") || k === " " || ["w","a","s","d"].includes(k)) {
        e.preventDefault();
      }

      if (k === "arrowup" || k === "w") state.nextDir = {x: 0, y: -1};
      else if (k === "arrowdown" || k === "s") state.nextDir = {x: 0, y: 1};
      else if (k === "arrowleft" || k === "a") state.nextDir = {x: -1, y: 0};
      else if (k === "arrowright" || k === "d") state.nextDir = {x: 1, y: 0};
      else if (k === " ") reset();
    }



    restartBtn?.addEventListener("click", () => {
      setDemoActive(true);
      reset();
    });
    window.addEventListener("keydown", onKey, { passive: false });

    // run
    reset();

    let last = performance.now();
    function loop(now) {
      const dt = (now - last) / 1000;
      last = now;

      state.t += dt;
      const interval = 1 / state.speed;
      while (state.t >= interval) {
        state.t -= interval;
        step();
      }

      draw();
      requestAnimationFrame(loop);
    }
    requestAnimationFrame(loop);

    console.log("[demo] loaded OK");
  } catch (err) {
    console.error("[demo] crashed:", err);
  }
})();

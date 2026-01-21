(() => {
  const canvas = document.getElementById("snakeCanvas");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");
  const W = canvas.width, H = canvas.height;

  // BIGGER grid blocks
  const cell = 28;
  const cols = Math.floor(W / cell);
  const rows = Math.floor(H / cell);

  const rand = (n) => Math.floor(Math.random() * n);

  const state = {
    snake: [{x: 6, y: 8}, {x: 5, y: 8}, {x: 4, y: 8}],
    dir: {x: 1, y: 0},
    food: {x: 14, y: 9},
    t: 0,
    speed: 5.8,
  };

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

  function drawBg() {
    ctx.clearRect(0,0,W,H);
    ctx.fillStyle = "rgba(0,0,0,.12)";
    ctx.fillRect(0,0,W,H);

    // subtle grid
    ctx.strokeStyle = "rgba(255,255,255,.05)";
    ctx.lineWidth = 1;

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

  function drawFood() {
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

  function drawSnake() {
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
  }

  function aiChooseDirection() {
    const head = state.snake[0];
    const options = [
      {x: 1, y: 0}, {x: -1, y: 0}, {x: 0, y: 1}, {x: 0, y: -1},
    ].filter(d => !(d.x === -state.dir.x && d.y === -state.dir.y));

    options.sort((a,b) => {
      const da = Math.abs((head.x + a.x) - state.food.x) + Math.abs((head.y + a.y) - state.food.y);
      const db = Math.abs((head.x + b.x) - state.food.x) + Math.abs((head.y + b.y) - state.food.y);
      return da - db;
    });

    for (const d of options) {
      const nx = (head.x + d.x + cols) % cols;
      const ny = (head.y + d.y + rows) % rows;
      const hit = state.snake.some((p, idx) => idx !== state.snake.length - 1 && p.x === nx && p.y === ny);
      if (!hit) return d;
    }
    return options[0] || state.dir;
  }

  function step() {
    state.dir = aiChooseDirection();

    const head = state.snake[0];
    const nx = (head.x + state.dir.x + cols) % cols;
    const ny = (head.y + state.dir.y + rows) % rows;

    const tail = state.snake[state.snake.length - 1];
    const hitSelf = state.snake.some((p) => p.x === nx && p.y === ny) && !(tail.x === nx && tail.y === ny);
    if (hitSelf) {
      state.snake = [{x: 6, y: 8}, {x: 5, y: 8}, {x: 4, y: 8}];
      state.dir = {x: 1, y: 0};
      placeFood();
      return;
    }

    state.snake.unshift({x: nx, y: ny});

    const ate = (nx === state.food.x && ny === state.food.y);
    if (ate) placeFood();
    else state.snake.pop();

    if (state.snake.length > 26) state.snake.pop();
  }

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

    drawBg();
    drawFood();
    drawSnake();

    requestAnimationFrame(loop);
  }

  placeFood();
  requestAnimationFrame(loop);
})();

import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  scenarios: {
    steady_load: {
      executor: "ramping-vus",
      startVUs: 20,
      stages: [
        { duration: "30s", target: 200 },
        { duration: "60s", target: 500 },
        { duration: "30s", target: 0 }
      ]
    }
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<800"]
  }
};

export default function () {
  const payload = JSON.stringify({
    sessionId: `s-${__VU}-${__ITER}`,
    userId: `u-${__VU}`,
    message: "请问退款流程是什么",
    channel: "web"
  });
  const headers = { "Content-Type": "application/json" };
  const res = http.post("http://localhost:8080/api/chat", payload, { headers });
  check(res, {
    "status is 200": (r) => r.status === 200,
    "has answer": (r) => r.body && r.body.includes("answer")
  });
  sleep(0.1);
}

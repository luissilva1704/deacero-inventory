import http from "k6/http";
import { check } from "k6";

export const options = {
  scenarios: {
    low_load: {
      executor: "constant-arrival-rate",
      rate: 10,             // ✅ 10 requests por segundo
      timeUnit: "1s",       // en 1 segundo
      duration: "30s",      // durante 30 segundos
      preAllocatedVUs: 5,   // VUs iniciales
      maxVUs: 20,           // máximo de VUs que k6 puede usar
    },
  },
};

const BASE_URL = "https://deacero-inventario-api-368147415867.us-central1.run.app";

export default function () {
  const res = http.get(`${BASE_URL}/deacero/api/v1/products`);

  check(res, {
    "status 200": (r) => r.status === 200,
  });
}

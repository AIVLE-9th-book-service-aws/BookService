const API_BASE_URL = "/api";

export async function apiFetch(path, options = {}) {
  const token = localStorage.getItem("accessToken");

  const headers = {
    ...(options.headers || {}),
  };

  if (!(options.body instanceof FormData)) {
    headers["Content-Type"] = headers["Content-Type"] || "application/json";
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    throw new Error("로그인이 필요한 기능입니다.");
  }

  if (response.status === 403) {
    throw new Error("권한이 없습니다.");
  }

  if (!response.ok) {
    let message = "요청 처리에 실패했습니다.";

    try {
      const data = await response.json();
      message = data.message || data.error || message;
    } catch {
      // JSON이 아니면 기본 메시지 사용
    }

    throw new Error(message);
  }

  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    return response.json();
  }

  return response.text();
}
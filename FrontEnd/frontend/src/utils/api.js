export const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api';

export async function fetchApi(path, options = {}) {
  const response = await fetch(`${BASE_URL}${path}`, options);
  return response;
}
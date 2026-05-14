import { useState, useEffect, useCallback, useRef } from 'react';
import { ApiError } from '@/api/client';

interface UseApiState<T> {
  data: T | null;
  isLoading: boolean;
  error: string | null;
  isUsingMock: boolean;
}

interface UseApiOptions<T> {
  /** Mock data to fall back to when the backend is unavailable */
  mockData?: T;
  /** Skip the API call entirely (e.g., when a param is missing) */
  skip?: boolean;
}

/**
 * Generic hook for fetching data from the backend with automatic mock fallback.
 * When the backend is unreachable (network error, 5xx, 401), the hook falls back
 * to the provided mock data so the UI always renders something useful.
 *
 * Uses refs for fetcher and mockData to avoid infinite re-render loops.
 * The `deps` array is serialized to a stable key for effect comparison.
 */
export function useApi<T>(
  fetcher: () => Promise<T>,
  deps: unknown[] = [],
  options: UseApiOptions<T> = {},
): UseApiState<T> & { refetch: () => void } {
  const { mockData, skip = false } = options;
  const [state, setState] = useState<UseApiState<T>>({
    data: null,
    isLoading: !skip,
    error: null,
    isUsingMock: false,
  });

  // Refs for values that should NOT trigger re-fetch when their identity changes
  const fetcherRef = useRef(fetcher);
  fetcherRef.current = fetcher;
  const mockDataRef = useRef(mockData);
  mockDataRef.current = mockData;

  // Serialize deps to a stable string for effect comparison
  const depsKey = JSON.stringify(deps);

  const execute = useCallback(async () => {
    if (skip) return;
    setState((prev) => ({ ...prev, isLoading: true, error: null }));
    try {
      const data = await fetcherRef.current();
      setState({ data, isLoading: false, error: null, isUsingMock: false });
    } catch (err) {
      const mock = mockDataRef.current;
      const isApiError = err instanceof ApiError;
      const isNetworkError = !isApiError;
      const isServerError = isApiError && err.status >= 500;

      if ((isNetworkError || isServerError) && mock !== undefined) {
        setState({
          data: mock,
          isLoading: false,
          error: null,
          isUsingMock: true,
        });
      } else {
        const message =
          err instanceof ApiError
            ? err.message
            : 'Something went wrong. Please try again.';
        setState({
          data: mock ?? null,
          isLoading: false,
          error: message,
          isUsingMock: mock !== undefined,
        });
      }
    }
  }, [skip, depsKey]); // eslint-disable-line react-hooks/exhaustive-deps

  useEffect(() => {
    execute();
  }, [execute]);

  return { ...state, refetch: execute };
}

/**
 * Wrapper for mutation calls (create, update, delete).
 * Returns an execute function + loading/error state.
 */
export function useMutation<TArgs extends unknown[], TResult>(
  mutationFn: (...args: TArgs) => Promise<TResult>,
) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fnRef = useRef(mutationFn);
  fnRef.current = mutationFn;

  const execute = useCallback(
    async (...args: TArgs): Promise<TResult> => {
      setIsLoading(true);
      setError(null);
      try {
        const result = await fnRef.current(...args);
        return result;
      } catch (err) {
        const message =
          err instanceof ApiError
            ? err.message
            : 'Something went wrong. Please try again.';
        setError(message);
        throw err;
      } finally {
        setIsLoading(false);
      }
    },
    [],
  );

  return { execute, isLoading, error };
}

import { createClient } from '@supabase/supabase-js';

export const SUPABASE_URL =
  process.env.NEXT_PUBLIC_SUPABASE_URL || 'https://hzoaklaiieydhltmafcj.supabase.co';
export const SUPABASE_ANON_KEY =
  process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ||
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh6b2FrbGFpaWV5ZGhsdG1hZmNqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM4NDA3NDQsImV4cCI6MjEwOTQxNjc0NH0.yOJs9MwHmrM16cdutHYVGri_UXKny0wEwlkUiaE_yNQ';

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY);
export const getSupabaseClient = () => supabase;
export default supabase;

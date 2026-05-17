create table if not exists users (
  id bigserial primary key,
  login varchar(100) unique not null,
  password_hash varchar(255) not null,
  created_at timestamp not null
);
create table if not exists projects (
  id bigserial primary key,
  user_id bigint not null references users(id) on delete cascade,
  title varchar(255) not null,
  original_file_name varchar(500),
  status varchar(50) not null,
  error_message text,
  duration_seconds double precision,
  created_at timestamp not null,
  updated_at timestamp not null
);
create table if not exists audio_files (
  id bigserial primary key,
  project_id bigint not null references projects(id) on delete cascade,
  kind varchar(30) not null,
  file_name varchar(500),
  content_type varchar(255),
  file_size bigint,
  file_data bytea not null,
  created_at timestamp not null
);
create table if not exists project_texts (
  id bigserial primary key,
  project_id bigint unique not null references projects(id) on delete cascade,
  raw_text text, processed_text text, ai_text text, manual_text text,
  created_at timestamp not null,
  updated_at timestamp not null
);
create table if not exists analysis_results (
  id bigserial primary key,
  project_id bigint unique not null references projects(id) on delete cascade,
  source_text_type varchar(30), word_count integer, sentence_count integer,
  paragraph_count integer, unique_word_count integer, average_sentence_length double precision,
  words_per_minute double precision, keywords_json jsonb, filler_words_json jsonb,
  summary text, created_at timestamp not null, updated_at timestamp not null
);
create table if not exists dictionaries (
  id bigserial primary key,
  user_id bigint references users(id) on delete cascade,
  name varchar(255) not null,
  type varchar(50) not null,
  is_system boolean not null default false,
  created_at timestamp not null
);
create table if not exists dictionary_entries (
  id bigserial primary key,
  dictionary_id bigint not null references dictionaries(id) on delete cascade,
  source_value varchar(500) not null,
  target_value varchar(500),
  enabled boolean not null default true,
  created_at timestamp not null
);
create table if not exists export_files (
  id bigserial primary key,
  project_id bigint not null references projects(id) on delete cascade,
  format varchar(20) not null,
  file_name varchar(500) not null,
  content_type varchar(255) not null,
  file_size bigint not null,
  file_data bytea not null,
  created_at timestamp not null
);

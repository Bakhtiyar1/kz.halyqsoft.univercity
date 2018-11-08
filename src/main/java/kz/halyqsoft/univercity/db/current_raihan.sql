ALTER TABLE pair_subject ADD column teacher_id BIGINT NULL;

CREATE TABLE trajectory(
  id        bigint       not null,
  type_name varchar(128) not null
);

ALTER TABLE trajectory
  ADD CONSTRAINT pk_trajectory PRIMARY KEY (id);

CREATE SEQUENCE S_TRAJECTORY
  MINVALUE 0
  START WITH 1
  NO CYCLE;

ALTER TABLE schedule_detail ADD COLUMN stream_id int;

ALTER TABLE schedule_detail
  ADD CONSTRAINT fk_t_schedule_detail_stream FOREIGN KEY (stream_id)
REFERENCES stream (id)
ON DELETE RESTRICT ON UPDATE RESTRICT;
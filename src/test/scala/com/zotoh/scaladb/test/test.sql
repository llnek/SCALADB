drop table if exists TESTTABLE;

create cached table TESTTABLE (
	CID bigint identity (100,1),
	NAME varchar(255) not null,
	AGE integer not null,
	WEIGHT double,
	BIRTHDATE date,
	BIRTHTSTAMP timestamp,
	DESC clob,
	SBIN binary,
	LBIN blob
);

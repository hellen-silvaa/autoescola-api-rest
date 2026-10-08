create table instrucoes (
    id bigint auto_increment,
    aluno_id bigint not null,
    instrutor_id bigint not null,
    data_hora datetime not null,

    primary key(id),
    constraint fk_instrucoes_aluno_id foreign key(aluno_id) references alunos(id),
    constraint fk_instrucoes_instrutor_id foreign key(instrutor_id) references instrutores(id)
);
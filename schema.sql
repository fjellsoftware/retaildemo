-- Postgres schema for empty database.

create table if not exists country
(
    country_id serial
    primary key,
    name       varchar not null
    unique
);

alter table country
    owner to postgres;

grant select, update, usage on sequence country_country_id_seq to application;

create index if not exists country_country_id_idx
    on country (country_id);

grant delete, insert, references, select, trigger, truncate, update on country to application;

create table if not exists user_account
(
    name            text                                              not null,
    username        text                                              not null
    constraint unique_email
    unique,
    hashed_password text                                              not null,
    created_at      timestamp with time zone default now()            not null,
    last_updated_at timestamp with time zone default now()            not null,
    role            text                     default 'CUSTOMER'::text not null
    constraint user_account_role_check
    check (role = ANY (ARRAY ['CUSTOMER'::text, 'STAFF'::text])),
    user_account_id uuid                                              not null
    primary key
    );

alter table user_account
    owner to postgres;

create index if not exists email_trigram_index
    on user_account using gin (username gin_trgm_ops);

create index if not exists name_trigram_index
    on user_account using gin (name gin_trgm_ops);

create index if not exists role_trigram_index
    on user_account using gin (role gin_trgm_ops);

create trigger user_account_update_last_updated
    before update
    on user_account
    for each row
    execute procedure func_update_last_updated_at();

grant delete, insert, references, select, trigger, truncate, update on user_account to application;

create table if not exists product
(
    product_id         serial
    primary key,
    stock_code         text                     default ''::text not null
    constraint unique_stock_code
    unique,
    description        text                                      not null,
    current_unit_price numeric(8, 2)                             not null,
    created_at         timestamp with time zone default now()    not null,
    last_updated_at    timestamp with time zone default now()    not null
    );

alter table product
    owner to postgres;

grant select, update, usage on sequence product_product_id_seq to application;

create index if not exists product_current_unit_price_idx
    on product (current_unit_price);

create index if not exists description_trigram_index
    on product using gin (description gin_trgm_ops);

create index if not exists product_current_unit_price_idx1
    on product (current_unit_price);

create index if not exists stock_code_trigram_index
    on product using gin (stock_code gin_trgm_ops);

create trigger product_update_last_updated
    before update
    on product
    for each row
    execute procedure func_update_last_updated_at();

grant delete, insert, references, select, trigger, truncate, update on product to application;

create table if not exists purchase_order
(
    purchase_order_id serial
    primary key,
    country_id        integer                                      not null
    references country,
    created_at        timestamp with time zone default now()       not null,
    last_updated_at   timestamp with time zone default now()       not null,
    order_status      text                     default 'NEW'::text not null
    constraint purchase_order_order_status_check
    check (order_status = ANY (ARRAY ['NEW'::text, 'CANCELLED'::text, 'SHIPPED'::text, 'IN_PROGRESS'::text])),
    address           text                                         not null,
    name              text                                         not null,
    phone_number      text                                         not null,
    customer_id       uuid
    constraint purchase_order_user_account_user_account_id_fk
    references user_account
    );

alter table purchase_order
    owner to postgres;

grant select, update, usage on sequence purchase_order_purchase_order_id_seq to application;

create table if not exists order_line
(
    purchase_order_id integer                                not null
    constraint fk_order_line__purchase_order
    references purchase_order,
    quantity          integer                                not null,
    unit_price        numeric(8, 2)                          not null,
    product_id        integer                                not null
    constraint fk_purchase_order__product
    references product
    on delete cascade,
    order_line_id     serial
    primary key,
    created_at        timestamp with time zone default now() not null
    );

alter table order_line
    owner to postgres;

grant select, update, usage on sequence order_line_order_line_id_seq to application;

create index if not exists order_line_purchase_order_id_idx
    on order_line (purchase_order_id);

create index if not exists order_line_product_id_idx
    on order_line (product_id);

grant delete, insert, references, select, trigger, truncate, update on order_line to application;

create index if not exists purchase_order_country_id_idx
    on purchase_order (country_id);

create index if not exists purchase_order_last_updated_at_idx
    on purchase_order (last_updated_at);

create index if not exists purchase_order_created_at_idx
    on purchase_order (created_at);

create index if not exists purchase_order_customer_id_index
    on purchase_order (customer_id);

create trigger purchase_order_update_last_updated
    before update
    on purchase_order
    for each row
    execute procedure func_update_last_updated_at();

grant delete, insert, references, select, trigger, truncate, update on purchase_order to application;

create table if not exists login_session
(
    login_session_id uuid                                   not null
    primary key,
    is_signed_out    boolean                  default false not null,
    created_at       timestamp with time zone default now() not null,
    last_updated_at  timestamp with time zone default now() not null,
    user_account_id  uuid                                   not null
    constraint login_session_user_account_user_account_id_fk
    references user_account
    );

alter table login_session
    owner to postgres;

create index if not exists login_session_last_updated_at_idx
    on login_session (last_updated_at);

create index if not exists login_session_created_at_idx
    on login_session (created_at);

create index if not exists login_session_user_account_id_index
    on login_session (user_account_id);

create trigger login_session_update_last_updated
    before update
    on login_session
    for each row
    execute procedure func_update_last_updated_at();

grant delete, insert, references, select, trigger, truncate, update on login_session to application;

create table if not exists metric_kind
(
    metric_kind_id bigserial
    primary key,
    name           text                                   not null
    unique,
    created_at     timestamp with time zone default now() not null
    );

alter table metric_kind
    owner to postgres;

grant select, update, usage on sequence metric_kind_metric_kind_id_seq to application;

grant delete, insert, references, select, trigger, truncate, update on metric_kind to application;

create table if not exists metric_value
(
    metric_value_id bigserial
    primary key,
    metric_kind_id  bigint                                 not null
    constraint metric_value_metric_kind_metric_kind_id_fk
    references metric_kind,
    value           double precision                       not null,
    created_at      timestamp with time zone default now() not null
    );

alter table metric_value
    owner to postgres;

grant select, update, usage on sequence metric_value_metric_value_id_seq to application;

grant delete, insert, references, select, trigger, truncate, update on metric_value to application;


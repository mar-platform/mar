import os
import sqlite3

def model_already_exists(model_id, c):
    c.execute('SELECT model_id FROM data WHERE model_id = ?', [model_id])
    value = c.fetchone()
    return value is not None

def open_db(output_folder, database_name, smash=False):
    db = os.path.join(output_folder, database_name)
    if smash and os.path.exists(db):
        os.remove(db)
        
    conn = sqlite3.connect(db)
    c = conn.cursor()    

    schema = os.path.join(os.path.dirname(os.path.abspath(__file__)), '../schema/crawlerdb.sql')

    with open(schema, 'r') as file:
        stms = file.read().split(";")
        for stm in stms:
            c.execute(stm)
        
    conn.commit()
    return conn

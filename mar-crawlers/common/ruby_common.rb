require 'sqlite3'

def open_database(output_file)
  schema = IO.read("../schema/crawlerdb.sql")
  # puts schema
  db = SQLite3::Database.new(output_file)
  schema.split(';').each do |part|
    db.execute(part)
  end
  yield(db)
  return db
end


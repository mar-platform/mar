##
## Reads "page files" downloaded from GenMyModel and
## given a list of downloaded models generate a csv file
## with its metadata
##
require 'csv'
require 'sqlite3'
require_relative 'indexer'
require_relative '../common/ruby_common'

def find_model(id, models)
  models.each { |m|
    project_id = m['projectId']
    if project_id == id 
      return m
    end
  }
  return nil
end

def insert_model(db, model_id, fname, name, link_href, description, creationDate, lastModificationDate, user)

  results = db.query('SELECT id FROM repo_info WHERE id = ?', user)
  repo = results.next
  if not repo
    db.execute('INSERT INTO repo_info(id) VALUES(?)', user)
  end

  insert = <<-SQL
         INSERT INTO data(model_id, filename, name, download_url, size, license,
                repo_id, creation_date, last_update, description) 
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) 
 SQL
  
  db.execute(insert, model_id, fname, name, link_href, -1, nil, user,
             creationDate, lastModificationDate, description)
end

if __FILE__ == $0
  if ARGV.size < 5
    puts "metadata_gen model-type extension pages-folder input_folder outputfile"
    exit
  end

  model_type = ARGV[0]
  extension = ARGV[1]
  folder = ARGV[2]
  file_list = Dir["#{ARGV[3]}/*"]
  output = ARGV[4]

  index = make_index(nil, nil, folder)
  if index.empty?
    puts "Empty pages folder. Please check"
    exit
  end
  
  models = index[model_type]
  
  # CSV.open(output, "w") do |csv|
  
  open_database(output) do |db|
    file_list.each do |filename|
      filename = filename.chomp
      fname = filename.split('/').last
      id = filename.split('/').last.sub('.' + extension, '')
      m = find_model(id, models)
      if m.nil?
        puts "Model #{filename} -- #{id} not found"
      else
        name = m['name']
        creation = m['creationDate']
        lastModification = m['lastModificationDate']
        user = m['spaceName']        
        description = m['description']
        
        link_href = nil
        link_map = m['links'].find { |l| l['rel'] == 'xmi' }
        if not link_map.nil?
          link_href = link_map['href']
        end
        
        # csv << [filename, name, link_href, user, creation, lastModification]

        puts "Inserting #{id}"
        insert_model(db, id, fname, name, link_href, description, creation, lastModification, user)

      end
    end
  end
end

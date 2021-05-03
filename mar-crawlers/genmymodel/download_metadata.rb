# https://stackoverflow.com/questions/12883650/https-requests-in-ruby
# sudo gem install curb
require 'curl'
require 'json'

LIMIT = 20

def query(page, limit)
  resp = Curl.get "https://app.genmymodel.com/api/projects/public?limit=#{limit}&page=#{page}"
  return resp.body_str
end

def save(string, idx, dir)
  name = "page.%08d" % [idx]
  File.write("#{dir}/#{name}", string)
end

if __FILE__ == $0
  if ARGV.size < 1
    puts "download_metadata.rb outputFolder [--restart]"
    exit
  end

  output = ARGV[0]
  restart = ARGV[1] == '--restart'

  puts "Query first page..."
  
  # Query the first one to determine the number of pages
  first = query(1, LIMIT)
  data = JSON.parse(first)
  pages = data["totalPages"].to_i

  if not restart
    # Files are in the form folder/page.000032 
    max = Dir.glob(output + '/page.*').
            map { |f| f.split('/').last.split('.').last.to_i }.max
    if max.nil?
      start = 0
    else
      start = max + 1
    end
  end
  
  (start..pages).each { |idx|
      puts "Query page #{idx} of #{pages}"
    str = query(idx, LIMIT)
    save(str, idx, output)
  }

  print(pages)
#print(data)
end


require 'json'

def process_json(json, index_by_type)
  elements = json['elements']
  elements.each { |e|
    type = e['type']
    index_by_type[type] ||= []
    index_by_type[type] << e
  }
end

def process_files(init=nil, last=nil, pages=nil)
  index = {}

  files = pages.nil? ? Dir["pages/*"] : Dir[pages + "/*"]
  files.each { |f|
    basename = File.basename(f)
    if basename.start_with?('page')
      #page.00005801
      if init != nil && last != nil
        num = basename.sub("page.", "").to_i
        if num < init || num > last
          next
        end
      end
      
      begin
        file = File.open f
        json = JSON.load file
        process_json(json, index)
      rescue => e
        # puts "Problem with #{f} - #{e.message}"
        # puts "Problem with #{f}"
      end
    end
  }

  index
end

def make_index(init=nil, last=nil, pages=nil)
  index = process_files(init, last, pages)
  index.each { |k, v|
    puts k + " - " + v.size.to_s
  }
  index
end

if __FILE__ == $0 
  init = nil
  last = nil
  if ARGV.size == 1
    parts = ARGV[0].split(":")
    init = parts[0].to_i
    last = parts[1].to_i
  end

  make_index(init, last)
end
